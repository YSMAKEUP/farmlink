package com.farmlink.users.dto;

import lombok.Getter;

@Getter
public class UserRequest {
    private String name;
    private String email;
    private String password;
    private String farmCode;
    private String farmName;
}