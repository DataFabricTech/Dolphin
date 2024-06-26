package com.mobigen.dolphin.repository.trino.extractor;

import lombok.Getter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class AbsResultSetExtractor<T> implements ResultSetExtractor<T> {
    final UUID jobId;
    @Getter
    final String prefix;
    final ClassPathResource dataPath;
    final ClassPathResource schemaPath;

    protected AbsResultSetExtractor(UUID jobId) {
        this.jobId = jobId;
        prefix = "dev/" + jobId;
        createDirectories(prefix);
        dataPath = new ClassPathResource(prefix + "/data.csv");
        schemaPath = new ClassPathResource(prefix + "/schema.json");
    }

    private void createDirectories(String prefix) {
        try {
            Files.createDirectories(Path.of(prefix));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
