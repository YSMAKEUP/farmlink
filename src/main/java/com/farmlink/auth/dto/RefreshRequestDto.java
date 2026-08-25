package com.farmlink.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshRequestDto {
    private String refreshToken;
}