package com.example.ktech_project3.user;

import com.example.ktech_project3.user.dto.UserDto;
import com.example.ktech_project3.user.repo.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Slf4j
@Service
public class UserService implements UserDetailsService {
    private final passwordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserService(passwordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    // user info check
    public UserDto creatUser(UserDto dto)
    {
        if(userRepository.existsByUsername(dto.getUsername()) ||
        !dto.getPassword().equals(dto.getPassCheck())
        ){
            throw new IllegalArgumentException("UserName or Password is not correct");
        }
        // CREATE USER

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


    }
}
