package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

@Configuration
public class SecretsManagerConfig {

    @Bean
    public String dbCredentials() {

       
        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(Region.AP_SOUTH_1)
                .build();

       
        GetSecretValueRequest request = GetSecretValueRequest.builder()
                .secretId("User-service")   // your secret name
                .build();

        
        String secretString = client.getSecretValue(request).secretString();

        return secretString;
    }
}
