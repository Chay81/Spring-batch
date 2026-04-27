package com.demo.springbatch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
@Slf4j
public class FileMoveTasklet implements Tasklet {

    @Value("${app.input-dir}")
    private String inputDir;

    @Value("${app.archive-dir}")
    private String archiveDir;

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution,
                                @NonNull ChunkContext chunkContext) throws Exception {

        File dir = new File(inputDir);
        File[] files = dir.listFiles((d, name) -> name.startsWith("users_"));

        if (files == null || files.length == 0) {
            log.warn("No files to move");
            return RepeatStatus.FINISHED;
        }

        for (File file : files) {
            Path source = file.toPath();
            Path target = Path.of(archiveDir, file.getName());

            Files.createDirectories(target.getParent());

            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);

            log.info("Moved file: {} → {}", source, target);
        }

        return RepeatStatus.FINISHED;
    }
}
