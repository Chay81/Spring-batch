package com.demo.springbatch.reader;

import com.demo.springbatch.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class UserItemReaderConfig {

    private final DataSource dataSource;

    @Bean
    @StepScope
    public FlatFileItemReader<User> reader(
            @Value("#{stepExecutionContext['fileName']}") Resource resource) {

        FlatFileItemReader<User> reader = new FlatFileItemReader<>();
//        reader.setResource(new ClassPathResource("users.csv"));
        reader.setResource(resource);
        reader.setLinesToSkip(1); // skip header

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("name", "email");

        BeanWrapperFieldSetMapper<User> mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(User.class);

        DefaultLineMapper<User> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(mapper);

        reader.setLineMapper(lineMapper);

        // LOGGING (this is your proof of partitioning)
        log.info("Thread: {} Processing File Range: {}",
                Thread.currentThread().getName(),
                resource.getFilename());

        return reader;
    }

}