package com.demo.springbatch.partitioner;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FilePartitioner implements Partitioner {

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {

        int totalLines = 100; // Assume we have 100 files to process
//        int totalLines = 1_000_000; // or calculate dynamically

        int targetSize = totalLines / gridSize;
        Map<String, ExecutionContext> result = new HashMap<>();
        int start = 1;

        for (int i = 0; i < gridSize; i++) {
            ExecutionContext context = new ExecutionContext();

            context.putInt("startLine", start);
            context.putInt("endLine", start + targetSize);
            context.putInt("totalPartitions", gridSize);

            result.put("partition" + i, context);
            start += targetSize;
        }

        return result;
    }
}

