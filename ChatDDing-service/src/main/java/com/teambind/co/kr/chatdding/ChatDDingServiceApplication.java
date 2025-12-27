package com.teambind.co.kr.chatdding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.teambind.co.kr.chatdding.infrastructure.persistence.mongodb.repository")
public class ChatDDingServiceApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(ChatDDingServiceApplication.class, args);
	}
	
}
