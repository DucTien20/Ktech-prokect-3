package com.example.ktech_project3.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "user_table")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity{

    private String username;
    private String password;
    private String nickname;
    private String fullName;
    private Integer ageGroup;
    private String email;
    private String phone;
    private String profileImgUrl;
    private String authorities;



    // 사용자 전환 신청 여부
    private boolean businessApplication;

    private boolean active;
    @OneToMany(mappedBy = "user",fetch = FetchType.LAZY)
    @JsonManagedReference
    private final List<Shop> shops = new ArrayList<>();

//    @PostLoad
//    public void updateActiveStatus() {
//        // Check the role and set the 'active' field accordingly
//        if (this.authorities.contains("ROLE_DEFAULT") || !this.authorities.isEmpty()) {
//            this.active = true;
//        } else {
//            this.active = false;
//        }
//    }
}




