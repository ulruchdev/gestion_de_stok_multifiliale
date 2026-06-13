package com.stockmaster.auth.dto.response;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InscriptionResponse {

    private String email;
    private Long groupId;
    private String message;
}
