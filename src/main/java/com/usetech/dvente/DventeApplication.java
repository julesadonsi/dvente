package com.usetech.dvente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DventeApplication {

	public static void main(String[] args) {
		SpringApplication.run(DventeApplication.class, args);
	}

}
