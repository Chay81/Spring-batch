package com.demo.springbatch.config;

import com.demo.springbatch.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

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