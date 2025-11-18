package com.condaty.auth_service.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Documento de identidad es requerido")
    @Size(max = 50, message = "Documento no puede exceder 50 caracteres")
    private String uuid; // CI, DNI, Pasaporte

    @NotBlank(message = "Nombre es requerido")
    @Size(max = 100, message = "Nombre no puede exceder 100 caracteres")
    private String firstName;

    @Size(max = 100, message = "Segundo nombre no puede exceder 100 caracteres")
    private String secondName;

    @NotBlank(message = "Apellido paterno es requerido")
    @Size(max = 100, message = "Apellido paterno no puede exceder 100 caracteres")
    private String paternalName;

    @Size(max = 100, message = "Apellido materno no puede exceder 100 caracteres")
    private String motherName;

    @Size(max = 20, message = "Teléfono no puede exceder 20 caracteres")
    private String phone;

    @Email(message = "Email debe ser válido")
    @Size(max = 150, message = "Email no puede exceder 150 caracteres")
    private String email; // OPCIONAL - puede ser null

    private LocalDate birthday;

    @Size(min = 6, message = "Password debe tener al menos 6 caracteres")
    private String password; // OPCIONAL - si es null, la persona no puede hacer login
}
