package com.demo.springbatch.writer;

import com.demo.springbatch.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class UserItemWriterConfig {

    private final DataSource dataSource;

    @Bean
    public JdbcBatchItemWriter<User> writer() {

        JdbcBatchItemWriter<User> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql("INSERT INTO users (name, email) VALUES (:name, :email)");
        writer.setDataSource(dataSource);
        writer.setAssertUpdates(false);
        writer.afterPropertiesSet(); // IMPORTANT
        return writer;
    }

    @Bean
    public ItemWriter<User> writerWithLogging(JdbcBatchItemWriter<User> writer) {
        return items -> {
            log.info("Thread {} wrote {} records in this chunk",
                    Thread.currentThread().getName(),
                    items.size());
            writer.write(items); // actual DB write happens here
        };
    }
}