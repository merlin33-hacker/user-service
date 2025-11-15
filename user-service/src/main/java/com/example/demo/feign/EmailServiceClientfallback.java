package com.example.demo.feign;

import java.util.logging.Logger;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EmailServiceClientfallback implements EmailServiceClient {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EmailServiceClientfallback.class);

	@Override
	public String sendOtp(String email) {
		log.error("email service is down -sendotp for {}",email);
		return  "Email services were down at this moment please try after some time";
	}

	@Override
	public Boolean verifyOtp(String email, String otp) {
		log.error("email service is down -verifyotp for {}",email);
		return false;
	}

	@Override
	public String generateforgotOtp(String email) {
		log.error("email service is down -geerateforgoototp for {}",email);
		return "Email services were down at this moment please try after some time";
	}

	@Override
	public Boolean forgotverifyOtp(String email, String otp) {
		log.error("email service is down -forgotverifyOtp for {}",email);
		return false;
	}

}
