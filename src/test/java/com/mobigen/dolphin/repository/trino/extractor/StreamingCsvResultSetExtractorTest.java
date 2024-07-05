package com.mobigen.dolphin.repository.trino.extractor;

import com.mobigen.dolphin.util.Pair;
import com.opencsv.CSVWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@ExtendWith(MockitoExtension.class)
class StreamingCsvResultSetExtractorTest {

    @Mock
    private ResultSet resultSet;
    @Mock
    private ResultSetMetaData resultSetMetaData;
    @InjectMocks
    private StreamingCsvResultSetExtractor extractor;

    @Test
    void testExtractData() throws SQLException {
        // Mock ResultSet
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString(1)).thenReturn("GTX Basic", "GTX Pro");
        when(resultSet.getString(2)).thenReturn("GTX", "GTX");
        when(resultSet.getInt(3)).thenReturn(550, 4821);

        // Mock ResultSetMetaData
        var columnNames = List.of(
                new Pair<>("product", 12), // Assuming 12 represents VARCHAR
                new Pair<>("series", 12),
                new Pair<>("sales_price", 4) // Assuming 4 represents INTEGER
        );
        when(resultSetMetaData.getColumnCount()).thenReturn(columnNames.size());
        for (var i = 0; i < columnNames.size(); i++) {
            when(resultSetMetaData.getColumnName(i + 1)).thenReturn(columnNames.get(i).left());
            when(resultSetMetaData.getColumnLabel(i + 1)).thenReturn(columnNames.get(i).left());
            when(resultSetMetaData.getColumnType(i + 1)).thenReturn(columnNames.get(i).right()); // Assuming 12 represents VARCHAR
        }

        doReturn(resultSetMetaData).when(resultSet).getMetaData();

        UUID jobId = UUID.randomUUID();
        extractor = new StreamingCsvResultSetExtractor(jobId, 3L);

        // Inject StringWriter instances
        var dataStringWriter = new StringWriter();
        var schemaStringWriter = new StringWriter();
        extractor.setDataWriter(new CSVWriter(dataStringWriter));
        extractor.setSchemaWriter(schemaStringWriter);

        extractor.extractData(resultSet);

        assertEquals("{" +
                        "\"schema\":{\"product\":\"TEXT\",\"series\":\"TEXT\",\"sales_price\":\"INTEGER\"}," +
                        "\"totalRows\":3" +
                        "}",
                schemaStringWriter.toString());
        assertEquals("""
                        "product","series","sales_price"
                        "GTX Basic","GTX","550"
                        "GTX Pro","GTX","4821"
                        """,
                dataStringWriter.toString());
    }
}