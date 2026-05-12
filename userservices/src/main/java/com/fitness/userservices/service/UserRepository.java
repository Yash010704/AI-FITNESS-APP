package com.fitness.userservices.service;

import com.fitness.userservices.models.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {


    boolean existsByEmail(String email);

    Boolean  existsByKeycloakId(String userId);

    User findByEmail( String email);
}
