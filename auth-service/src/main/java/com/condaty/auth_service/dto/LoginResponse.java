package com.condaty.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String type;
    private String uuid;
    private String firstName;
    private String paternalName;
    private String email;
    private Long expiresIn;

    public LoginResponse(String token, String uuid, String firstName, String paternalName, String email, Long expiresIn) {
        this.token = token;
        this.type = "Bearer";
        this.uuid = uuid;
        this.firstName = firstName;
        this.paternalName = paternalName;
        this.email = email;
        this.expiresIn = expiresIn;
    }
}
