package de.caransgar.chorehub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChorehubApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChorehubApplication.class, args);
	}

}
