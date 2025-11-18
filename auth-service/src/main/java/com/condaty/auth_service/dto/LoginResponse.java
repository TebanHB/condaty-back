package com.condaty.auth_service.dto;

import java.time.LocalDate;
import java.util.List;

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
    private String secondName;
    private String paternalName;
    private String motherName;
    private String phone;
    private String email;
    private LocalDate birthday;
    private Long expiresIn;
    private List<TenantDTO> tenants;

    public LoginResponse(String token, String uuid, String firstName, String secondName, String paternalName, String motherName,
                         String phone, String email, LocalDate birthday, Long expiresIn, List<TenantDTO> tenants) {
        this.token = token;
        this.type = "Bearer";
        this.uuid = uuid;
        this.firstName = firstName;
        this.secondName = secondName;
        this.paternalName = paternalName;
        this.motherName = motherName;
        this.phone = phone;
        this.email = email;
        this.birthday = birthday;
        this.expiresIn = expiresIn;
        this.tenants = tenants;
    }
}
