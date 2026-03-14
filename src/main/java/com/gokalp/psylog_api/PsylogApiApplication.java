package com.gokalp.psylog_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PsylogApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(PsylogApiApplication.class, args);
	}

}
