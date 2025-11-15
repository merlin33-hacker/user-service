package com.example.demo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.AuthRequest;
import com.example.demo.entity.AuthResponse;
import com.example.demo.entity.UserDto;
import com.example.demo.service.UserService;

@RestController
@RequestMapping("users")
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public String  registerUser(@Valid @RequestBody UserDto userDTO) {
    	 return userService.registerUser(userDTO);
       
    }
    @PostMapping("/verify")
    public String verify(@RequestParam("email") String email,
                         @RequestParam("otp") String otp) {
        return userService.verifyOtp(email, otp);
    }
  @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userService.logout(token));
    }

    @GetMapping("/home")
    public String home(  ) {
        return "Welcome to the Herbal Oil App!";
    }
    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email) {
        return userService.forgotPassword(email);
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam ("email")String email,
                                @RequestParam("otp") String otp,
                                @RequestParam("password") String newPassword) {
        return userService.resetPassword(email, otp, newPassword);
    }
    
    }
