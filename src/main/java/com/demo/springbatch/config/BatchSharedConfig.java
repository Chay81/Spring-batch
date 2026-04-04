package com.demo.springbatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class BatchSharedConfig {

    @Bean
    public Set<String> globalEmailSet() {
        return ConcurrentHashMap.newKeySet();
    }
}
