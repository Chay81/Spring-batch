package com.demo.springbatch.config;

import com.demo.springbatch.model.User;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class DuplicateSkippingProcessor implements ItemProcessor<User, User> {

    // thread-safe set for multi-threaded step
    private final Set<String> seenEmails = ConcurrentHashMap.newKeySet();
    private final MeterRegistry meterRegistry;

    @Override
    public User process(User item) {
        if (!seenEmails.add(item.getEmail())) {
            meterRegistry.counter("batch.duplicates.skipped").increment();
//            synchronized(System.out) {
//                log.info("Skipping duplicate: {} by thread {}", item.getEmail(),
//                        Thread.currentThread().getName());
//                System.out.flush();
//            }
            return null;
        }
        return item;
    }
}