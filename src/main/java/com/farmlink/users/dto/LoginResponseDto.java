package com.farmlink.users.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LoginResponseDto {

    private Long id;
    private String email;
    private String name;
    private String farmName;
    private String accessToken;
    private String refreshToken;



    @Builder
    private LoginResponseDto (Long id, String email, String name, String farmName, String accessToken, String refreshToken){

        this.id = id;
        this.email = email;
        this.name = name;
        this.farmName = farmName;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;


    }

}
