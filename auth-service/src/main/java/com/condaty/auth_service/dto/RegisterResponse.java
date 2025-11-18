package com.condaty.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {

    private String uuid;
    private String firstName;
    private String paternalName;
    private String email;
    private String message;
    private boolean success;
}
