package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling user related operations.
 */
@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Endpoint to create a new user.
     *
     * @param payload The payload containing the user's name and email.
     * @return The ID of the created user.
     */
    @PutMapping("/user")
    public ResponseEntity<Integer> createUser(@RequestBody CreateUserPayload payload) {
        User user = new User();
        user.setName(payload.getName());
        user.setEmail(payload.getEmail());

        User savedUser = userRepository.save(user);

        return ResponseEntity.ok(savedUser.getId());
    }

    /**
     * Endpoint to delete a user.
     *
     * @param userId The ID of the user to be deleted.
     * @return A response entity indicating the result of the operation.
     */
    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestParam int userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.badRequest().body("User with the given ID does not exist");
        }
    }
}