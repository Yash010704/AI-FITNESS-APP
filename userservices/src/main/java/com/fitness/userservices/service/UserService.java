package com.fitness.userservices.service;


import com.fitness.userservices.dto.RegisterRequest;
import com.fitness.userservices.dto.UserResponse;
import com.fitness.userservices.models.User;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {

    private final  UserRepository repository;

    public UserResponse register(RegisterRequest request) {

        if(repository.existsByEmail(request.getEmail())){
            User existingUser = repository.findByEmail(request.getEmail());
            UserResponse response = new UserResponse();
            response.setId(existingUser.getId());
            response.setPassword(existingUser.getPassword());
            response.setEmail(existingUser.getEmail());
            response.setFirstName(existingUser.getFirstName());
            response.setLastName(existingUser.getLastName());
            response.setCreatedAt(existingUser.getCreatedAt());
            response.setUpdatedAt(existingUser.getUpdatedAt());


            return response;
        }

        User user =  new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setKeycloakId(request.getKeycloakId());
        user.setLastName(request.getLastName());
        user.setPassword(request.getPassword());



        User SavedUser = repository.save(user);
        UserResponse response = new UserResponse();
        response.setId(SavedUser.getId());
        response.setPassword(SavedUser.getPassword());
        response.setKeycloakid(SavedUser.getKeycloakId());
        response.setEmail(SavedUser.getEmail());
        response.setFirstName(SavedUser.getFirstName());
        response.setLastName(SavedUser.getLastName());
        response.setCreatedAt(SavedUser.getCreatedAt());
        response.setUpdatedAt(SavedUser.getUpdatedAt());


        return response;
    }

    public UserResponse getUserProfile(String userId) {

        User user = repository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setPassword(user.getPassword());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        return response;
    }

    public Boolean existByUserId(String userId) {

        log.info("calling user service for{}" , userId);
        return repository.existsByKeycloakId(userId);
    }
}
