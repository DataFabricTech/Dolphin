package com.mobigen.dolphin.repository.trino.extractor;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
class ResultSetExtractorFactoryTest {

    @Test
    void testCreateResultSetExtractor() {
        var id = UUID.randomUUID();
        var extractor = ResultSetExtractorFactory.createResultSetExtractor(ExtractType.CSV, id);
        assertInstanceOf(StreamingCsvResultSetExtractor.class, extractor);
    }
}