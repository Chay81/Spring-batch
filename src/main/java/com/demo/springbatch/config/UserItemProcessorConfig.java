package com.demo.springbatch.config;

import com.demo.springbatch.model.User;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserItemProcessorConfig {

    @Bean
    public ItemProcessor<User, User> processor() {
        return user -> {
            // Example transformation (optional)
            user.setName(user.getName().toUpperCase());
            // DO NOT touch user.getId() → DB will generate
            return user;
        };
    }
}