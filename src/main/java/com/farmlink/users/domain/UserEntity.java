package com.farmlink.users.domain;

import com.farmlink.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users", indexes = @Index(name = "idx_users_farm_code", columnList = "farm_code"))
public class UserEntity  extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String farmCode;

    @Column(nullable = false)
    private String farmName;

    @Builder
    private UserEntity(String name, String email, String password, String farmCode, String farmName) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.farmCode = farmCode;
        this.farmName = farmName;
    }
}




