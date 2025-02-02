package com.unir.slrassistant.security;

import com.unir.slrassistant.security.model.request.RegisterRequest;
import com.unir.slrassistant.security.service.AuthenticationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SecurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecurityApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(
			AuthenticationService service
	) {
		return args -> {
			var admin = RegisterRequest.builder()
					.name("Admin User")
					.email("admin@admin.com")
					.password("password")
					.build();
			System.out.println("Admin token: " + service.register(admin).getToken());
		};
	}
}
