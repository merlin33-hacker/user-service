package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.UserEntity;
@Repository
public interface  UserRepository extends JpaRepository<UserEntity,Long> {
	Optional<UserEntity>  findByEmail(String email);
	 Optional<UserEntity>  findByPhoneNumber(String phoneNumber);
	 boolean existsByEmail(String email);
	 

}
