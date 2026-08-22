package com.farmlink.users.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;

@Getter
public class SignUpRequestDto {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String name;

    @NotBlank
    @Size(min = 8, max = 20)
    private String password;

    @NotBlank
    private String farmCode;

    @NotBlank
    private String farmName;

}