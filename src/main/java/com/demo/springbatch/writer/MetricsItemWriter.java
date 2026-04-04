package com.demo.springbatch.writer;

import com.demo.springbatch.model.User;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import io.micrometer.core.instrument.Counter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsItemWriter implements ItemWriter<User> {

    private final JdbcBatchItemWriter<User> delegate;
    private final MeterRegistry meterRegistry;

    // Track unique threads
    private final Set<String> threadNames = ConcurrentHashMap.newKeySet();

    // Track active thread count (approx)
    private final AtomicInteger activeThreads = new AtomicInteger(0);

    // Create counters ONCE
    private Counter totalWritesCounter;

    @PostConstruct
    public void init() {
//        totalWritesCounter = meterRegistry.counter("batch_db_writes_total");

        // Register gauges ONCE
        meterRegistry.gauge("batch.active.threads", activeThreads);
        log.info("Registered active threads gauge {}", activeThreads);
        meterRegistry.gauge("batch.unique.threads", threadNames, Set::size);
    }

    @Override
    public void write(Chunk<? extends User> chunk) throws Exception {

        String threadName = Thread.currentThread().getName();

        threadNames.add(threadName);
        activeThreads.incrementAndGet();

        try {
            log.info("Thread: {} Writing {} records",
                    threadName, chunk.size());

            // Actual DB write
            delegate.write(chunk);
            // FAST + THREAD SAFE
            totalWritesCounter.increment(chunk.size());

            // TOTAL DB WRITES
            meterRegistry.counter("batch_db_writes_total",
                            "thread", threadName).increment(chunk.size());
            log.info("Metrics Writer HIT by thread: {}", Thread.currentThread().getName());

            meterRegistry.counter("batch_db_writes_by_thread",
                            "thread", threadName).increment(chunk.size());

        } finally {
            activeThreads.decrementAndGet();

        }
    }
}
