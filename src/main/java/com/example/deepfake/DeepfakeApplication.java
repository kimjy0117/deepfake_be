package com.example.deepfake;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.deepfake")
public class DeepfakeApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeepfakeApplication.class, args);
	}

}
