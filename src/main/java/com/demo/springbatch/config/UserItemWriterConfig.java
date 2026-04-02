package com.demo.springbatch.config;

import com.demo.springbatch.model.User;
import io.micrometer.core.instrument.MeterRegistry;
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
        writer.afterPropertiesSet(); // IMPORTANT
        return writer;
    }

    @Bean
    public ItemWriter<User> loggingWriter(JdbcBatchItemWriter<User> writer) {
        return items -> {
            writer.write(items); // write to DB
            log.info("Thread {} wrote {} records in this chunk",
                    Thread.currentThread().getName(),
                    items.size());
        };
    }

    @Bean
    public ItemWriter<User> metricsWriter(JdbcBatchItemWriter<User> jdbcWriter,
                                   MeterRegistry meterRegistry) {
        return new MetricsItemWriter(jdbcWriter, meterRegistry);
    }
}