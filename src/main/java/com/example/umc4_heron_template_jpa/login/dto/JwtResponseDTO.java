package com.example.umc4_heron_template_jpa.login.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class JwtResponseDTO {
    @Builder
    @Getter
    @AllArgsConstructor
    public static class TokenInfo {
        private String accessToken;
        private String refreshToken;
    }
}
