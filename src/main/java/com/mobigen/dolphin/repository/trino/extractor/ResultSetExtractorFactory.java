package com.mobigen.dolphin.repository.trino.extractor;

import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class ResultSetExtractorFactory {
    public static AbsResultSetExtractor<Void> createResultSetExtractor(ExtractType extractType, UUID jobId) {
        switch (extractType) {
            default -> {
                return new StreamingCsvResultSetExtractor(jobId);
            }
        }
    }
}
