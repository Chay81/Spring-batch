package com.demo.springbatch.partitioner;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Deprecated
public class IdRangePartitioner implements Partitioner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {

        Map<String, ExecutionContext> result = new HashMap<>();

        Long min = jdbcTemplate.queryForObject("SELECT MIN(id) FROM users", Long.class);
        Long max = jdbcTemplate.queryForObject("SELECT MAX(id) FROM users", Long.class);

        long targetSize = (max - min) / gridSize + 1;

        long start = min;
        long end = start + targetSize - 1;

        for (int i = 0; i < gridSize; i++) {

            ExecutionContext context = new ExecutionContext();
            context.putLong("minId", start);
            context.putLong("maxId", end);

            result.put("partition" + i, context);

            start = end + 1;
            end = start + targetSize - 1;

            if (end > max) {
                end = max;
            }
        }

        return result;
    }
}
