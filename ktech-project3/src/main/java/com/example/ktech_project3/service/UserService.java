package com.example.ktech_project3.service;


import com.example.project_3.dto.UserDto;
import com.example.project_3.entity.User;
import com.example.project_3.repo.ShopRepository;
import com.example.project_3.repo.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service

public class UserService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;
    private final ShopRepository shopRepository;

    public UserService(PasswordEncoder passwordEncoder, UserRepository repository, ShopRepository shopRepository){
        this.passwordEncoder = passwordEncoder;
        this.repository = repository;
        this.shopRepository = shopRepository;

    }

    // CREATE
    public UserDto createUser(UserDto dto
    ) {
        if (repository.existsByUsername(dto.getUsername()) ||
                !dto.getPassword().equals(dto.getPassCheck()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        User newUser = new User();
        newUser.setUsername(dto.getUsername());
        newUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        newUser.setAuthorities("ROLE_DEFAULT");
        newUser.setActive(false);
        return UserDto.fromEntity(repository.save(newUser));
    }
    public UserDto getCurrentUserProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = repository.findByUsername(username);
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        User currentUser = user.get();
        System.out.println("Authorities: " + currentUser.getAuthorities()); // Log the authorities

        return UserDto.fromEntity(currentUser);
    }
    // read by username
    public UserDto getUserByUsername(String username) {
        Optional<User> user = repository.findByUsername(username);
        if (user.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return UserDto.fromEntity(user.get());
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optionalUser =
                repository.findByUsername(username);
        if (optionalUser.isEmpty())
            throw new UsernameNotFoundException(username);
        User user = optionalUser.get();
        String[] authorities = user.getAuthorities().split(",");

        return org.springframework.security.core.userdetails.User.withUsername(username)
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }
    // UPDATE
    public UserDto updateProfile(Long id, UserDto dto){
        Optional<User> optionalUser = repository.findById(id);
        User target = optionalUser.get();
        target.setNickname(dto.getNickname());
        target.setFullName(dto.getFullName());
        target.setEmail(dto.getEmail());
        target.setPhone(dto.getPhone());
        target.setAgeGroup(dto.getAgeGroup());
        if (dto.getNickname() == null ||
                dto.getFullName() == null ||
                dto.getEmail() == null ||
                dto.getPhone() == null ||
                dto.getAgeGroup() == null
        ){
            target.setAuthorities("ROLE_DEFAULT");
        }
        target.setAuthorities("ROLE_USER");
        target.setActive(true);
        return UserDto.fromEntity(repository.save(target));
    }
    // UPDATE profileImg
    public UserDto updateImg(Long id, MultipartFile image){
        Optional<User> optionalUser = repository.findById(id);
        if (optionalUser.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        String profileDir = "src/main/resources/static/media/" + id + "/";
        try {
            Files.createDirectories(Path.of(profileDir));
        }catch (IOException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String originalFileName = image.getOriginalFilename();
        String[] fileNameSplit = originalFileName.split("\\.");
        String extension = fileNameSplit[fileNameSplit.length - 1];
        String uploadPath = profileDir + "profile." + extension;
        try {
            BufferedImage bufferedImage = ImageIO.read(image.getInputStream());

            // Define crop dimensions (e.g., 200x200)
            int cropWidth = 200;
            int cropHeight = 200;

            BufferedImage croppedImage = new BufferedImage(cropWidth, cropHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = croppedImage.createGraphics();

            // Calculate the center of the original image
            int x = (bufferedImage.getWidth() - cropWidth) / 2;
            int y = (bufferedImage.getHeight() - cropHeight) / 2;

            g2d.drawImage(bufferedImage, 0, 0, cropWidth, cropHeight, x, y, x + cropWidth, y + cropHeight, null);
            g2d.dispose();

            ImageIO.write(croppedImage, extension, new File(uploadPath));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String reqPath = "/media/" + id + "/profile." + extension;
        User targetImg = optionalUser.get();
        targetImg.setProfileImgUrl(reqPath);
        return UserDto.fromEntity(repository.save(targetImg));
    }
    public void delete(Long id){

        if (repository.existsById(id))
            repository.deleteById(id);
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    public void applyForBusiness(Long id){
        Optional<User> user = repository.findById(id);

        if (user.isPresent() && !"ROLE_USER".equals(user.get().getAuthorities())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only users with role USER can apply for business");
        }
        user.get().setBusinessApplication(true);
        repository.save(user.get());
    }
    public List<User> getBusinessApplications(){
        checkIfAdmin();
        return repository.findByBusinessApplicationTrueAndAuthorities("ROLE_USER");
    }
    public void approveBusinessApplication(Long id){
        checkIfAdmin();
        Optional<User> user = repository.findById(id);
        if (user.isPresent() && user.get().isBusinessApplication() && "ROLE_USER".equals(user.get().getAuthorities())) {
            user.get().setAuthorities("ROLE_BUSINESS");
            user.get().setBusinessApplication(false);
            repository.save(user.get());
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot approve application for this user");
        }
    }
    public void rejectBusinessApplication(Long id){
        checkIfAdmin();
        Optional<User> user = repository.findById(id);
        if (user.isPresent() && user.get().isBusinessApplication() && "ROLE_USER".equals(user.get().getAuthorities())) {
            user.get().setBusinessApplication(false);
            repository.save(user.get());
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot reject application for this user");
        }
    }
    private void checkIfAdmin() {
        String currentUserRole = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();
        if (!currentUserRole.contains("ROLE_ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have admin privileges");
        }
    }




}
