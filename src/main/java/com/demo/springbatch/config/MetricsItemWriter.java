package com.demo.springbatch.config;

import com.demo.springbatch.model.User;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class MetricsItemWriter implements ItemWriter<User> {

    private final JdbcBatchItemWriter<User> delegate;
    private final MeterRegistry meterRegistry;

    // Track unique threads
    private final Set<String> threadNames = ConcurrentHashMap.newKeySet();

    // Track active thread count (approx)
    private final AtomicInteger activeThreads = new AtomicInteger(0);

    @Override
    public void write(Chunk<? extends User> chunk) throws Exception {

        String threadName = Thread.currentThread().getName();

        threadNames.add(threadName);
        activeThreads.incrementAndGet();

        try {
            // Actual DB write
            delegate.write(chunk);

            int size = chunk.size();

            // TOTAL DB WRITES
            meterRegistry.counter("batch.db.writes.total")
                    .increment(size);

            // PER THREAD METRIC
            meterRegistry.counter(
                    "batch.db.writes.by.thread",
                    "thread", threadName
            ).increment(size);

        } finally {
            activeThreads.decrementAndGet();

            // ACTIVE THREAD GAUGE
            meterRegistry.gauge(
                    "batch.active.threads",
                    activeThreads
            );

            // UNIQUE THREAD COUNT
            meterRegistry.gauge(
                    "batch.unique.threads",
                    threadNames,
                    Set::size
            );
        }
    }
}
