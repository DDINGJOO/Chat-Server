package com.teambind.co.kr.chatdding.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.teambind.co.kr.chatdding.adapter.out.persistence.repository")
public class MongoConfig {

}
