package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Configuration
public class DatabaseConfig {
@Autowired 
private String dbCredentials;
@PostConstruct
public void init() throws Exception{
	ObjectMapper mapper=new ObjectMapper();
JsonNode json=mapper.readTree(dbCredentials);
String username=json.get("Username").asText();
String password=json.get("passwored").asText();
System.setProperty("spring.datasource.username",username);
System.setProperty("spring.datasource.password",password);
}

}
