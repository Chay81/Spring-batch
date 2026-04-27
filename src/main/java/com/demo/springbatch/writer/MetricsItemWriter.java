package com.demo.springbatch.writer;

import com.demo.springbatch.model.User;
import com.demo.springbatch.repository.FileMetadataRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsItemWriter implements ItemWriter<User> {

    private final JdbcBatchItemWriter<User> delegate;
    private final MeterRegistry meterRegistry;
    private final Set<String> threadNames = ConcurrentHashMap.newKeySet();
    private final AtomicInteger activeThreads = new AtomicInteger(0);
    private final FileMetadataRepository repository;

    // Create counters ONCE
    private Counter totalWritesCounter;

    @PostConstruct
    public void init() {
        totalWritesCounter = meterRegistry.counter("batch_db_writes_total");

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
//            meterRegistry.counter("batch_db_writes_total",
//                            "thread", threadName).increment(chunk.size());
            log.info("Metrics Writer HIT by thread: {}", Thread.currentThread().getName());

            meterRegistry.counter("batch_db_writes_by_thread",
                            "thread", threadName).increment(chunk.size());

            // METADATA UPDATE START

            StepExecution stepExecution =
                    StepSynchronizationManager.getContext().getStepExecution();

            if (stepExecution != null) {

                String filePath = stepExecution
                        .getExecutionContext()
                        .getString("file");

                if (filePath != null) {
                    String fileName = new File(filePath).getName(); // IMPORTANT FIX

                    repository.findByFileName(fileName).ifPresent(meta -> {
                        int current = meta.getRecordCount() == null ? 0 : meta.getRecordCount();
                        meta.setRecordCount(current + chunk.size());
                        meta.setUpdatedAt(LocalDateTime.now());
                        repository.save(meta);
                    });
                }
            }

            // METADATA UPDATE END

        } finally {
            activeThreads.decrementAndGet();

        }
    }
}
