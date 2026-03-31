package com.demo.springbatch.config;

import com.demo.springbatch.model.User;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
@RequiredArgsConstructor
public class DuplicateSkippingProcessor implements ItemProcessor<User, User> {

    // thread-safe set for multi-threaded step
    private final Set<String> seenEmails = ConcurrentHashMap.newKeySet();
    private final MeterRegistry meterRegistry;
    private final AtomicLong duplicateCounter = new AtomicLong(0);

    @Override
    public User process(User item) {
        if (!seenEmails.add(item.getEmail())) {
            long count = duplicateCounter.incrementAndGet();

            // Log duplicate with thread info
            log.info("Skipping duplicate: {} | duplicate count: {} | thread: {}",
                    item.getEmail(), count, Thread.currentThread().getName());
            // Increment Prometheus counter
            meterRegistry.counter(
                    "batch.duplicates.skipped",   // Actuator-friendly name
                    "thread", Thread.currentThread().getName(),
                    "step", "userStep"
            ).increment();

//            synchronized(System.out) {
//                log.info("Skipping duplicate: {} by thread {}", item.getEmail(),
//                        Thread.currentThread().getName());
//                System.out.flush();
//            }
            return null;
        }
        return item;
    }

    public long getDuplicateCount() {
        return duplicateCounter.get();
    }
}