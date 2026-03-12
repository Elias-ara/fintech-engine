package com.fintechengine.modules.user.controller;

import com.fintechengine.modules.user.dto.CreateUserRequest;
import com.fintechengine.modules.user.dto.UserResponse;
import com.fintechengine.modules.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable UUID id) {
        return userService.findById(id);
    }
}
