package com.fitness.userservices.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @Email(message = "Invalid email format")
    private String email;
    private String keycloakId;

    @Size(min = 6 , message = "Password must have length of 6 ")
    private String password;
    private String firstName;
    private String lastName;
}