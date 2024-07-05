package com.mobigen.dolphin.repository.trino.extractor;

import com.mobigen.dolphin.util.Functions;
import com.opencsv.CSVWriter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.dao.DataAccessException;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
@Setter
public class StreamingCsvResultSetExtractor extends AbsResultSetExtractor<Void> {
    private CSVWriter dataWriter;
    private Writer schemaWriter;
    private Long totalRows;

    public StreamingCsvResultSetExtractor(UUID jobId, Long totalRows) {
        super(jobId);
        this.totalRows = totalRows;
    }

    @Override
    public Void extractData(final ResultSet rs) throws SQLException, DataAccessException {
        openWriter();
        try {
            var resultSetMetadata = rs.getMetaData();
            var columnCount = resultSetMetadata.getColumnCount();
            var schemaJson = new JSONObject();
            for (int i = 0; i < columnCount; i++) {
                var columnName = resultSetMetadata.getColumnName(i + 1);
                var columnType = Functions.getDolphinType(resultSetMetadata.getColumnType(i + 1));
                schemaJson.put(columnName, columnType.value());
            }
            var rootJson = new JSONObject();
            rootJson.put("totalRows", totalRows);
            rootJson.put("schema", schemaJson);
            schemaWriter.write(rootJson.toJSONString());
            dataWriter.writeAll(rs, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeQuietly(dataWriter);
            closeQuietly(schemaWriter);
        }
        return null;
    }

    void openWriter() {
        try {
            if (dataWriter == null) {
                createDirectories(prefix);
                this.dataWriter = new CSVWriter(new FileWriter(dataPath.getPath()));
            }
            if (schemaWriter == null) {
                createDirectories(prefix);
                this.schemaWriter = new FileWriter(schemaPath.getPath());
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
                // Ignore exception on close
            }
        }
    }
}
