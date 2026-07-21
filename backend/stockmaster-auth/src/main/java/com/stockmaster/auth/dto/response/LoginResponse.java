package com.stockmaster.auth.dto.response;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private String role;
    private String scope;
}
