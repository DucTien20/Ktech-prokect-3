package com.example.ktech_project3.user;

import com.example.ktech_project3.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

import java.util.ArrayList;

@Getter
@Setter
@Entity
@Table(name = "user_table")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    private String username;
    private String password;
    private String nickname;
    private String fullName;
    private Integer ageGroup;
    private String email;
    private String phone;

    private boolean businessApplication;

    private boolean active;
    @OneToMany(mappedBy = "user",fetch = FetchType.LAZY)
    @JsonManagedReference
    private final List<Shop> shops = new ArrayList<>();
}
