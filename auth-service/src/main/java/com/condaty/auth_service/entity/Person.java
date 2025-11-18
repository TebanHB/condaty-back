package com.condaty.auth_service.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "persons")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Person {

    @Id
    @Column(name = "uuid", length = 50, nullable = false)
    private String uuid; // Documento de identidad (CI, DNI, Pasaporte)

    @Column(name = "first_name", length = 100, nullable = false)
    private String firstName;

    @Column(name = "second_name", length = 100)
    private String secondName;

    @Column(name = "paternal_name", length = 100, nullable = false)
    private String paternalName;

    @Column(name = "mother_name", length = 100)
    private String motherName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 150, unique = true)
    private String email;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_active", nullable = false, insertable = false)
    private Boolean isActive;
}
