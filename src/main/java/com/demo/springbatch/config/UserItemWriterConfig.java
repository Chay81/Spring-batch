package com.demo.springbatch.config;

import com.demo.springbatch.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class UserItemWriterConfig {

    private final DataSource dataSource;

    @Bean
    public JdbcBatchItemWriter<User> writer() {

        JdbcBatchItemWriter<User> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql("INSERT INTO users (name, email) VALUES (:name, :email)");
        writer.setDataSource(dataSource);
        return writer;
    }
}