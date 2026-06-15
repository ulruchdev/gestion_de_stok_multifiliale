package com.stockmaster.auth.dto.response;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshTokenResponse {

    private String accessToken;
    private long expiresIn;
}
