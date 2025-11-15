package com.example.demo.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "email-service", url = "http://localhost:8083/otp",fallback=EmailServiceClientfallback.class)
public interface EmailServiceClient {

    @PostMapping("/generate")
    String sendOtp(@RequestParam("email") String email);

    @PostMapping("/verify")
    Boolean verifyOtp(@RequestParam("email") String email,
                      @RequestParam("otp") String otp);
    @PostMapping("/forgot-otp")
    String generateforgotOtp(@RequestParam("email") String email); 
    @PostMapping("/forgototp-verify")
    Boolean forgotverifyOtp(@RequestParam("email") String email,
            @RequestParam("otp") String otp);
 
}
