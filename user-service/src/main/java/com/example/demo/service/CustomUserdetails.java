package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.entity.UserEntity;
import com.example.demo.repository.UserRepository;
@Service
public class CustomUserdetails implements  UserDetailsService {
	   @Autowired
	    private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserEntity user=userRepository.findByEmail(email).orElseThrow(() ->new UsernameNotFoundException("User not found with email: " + email));
		return User.builder()
				.username(user.getEmail())
				.password(user.getPassword())
				.roles("USER")
				.build();
	}

}
