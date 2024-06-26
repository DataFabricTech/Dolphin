package com.mobigen.dolphin.repository.trino.extractor;

import com.mobigen.dolphin.util.Functions;
import com.opencsv.CSVWriter;
import org.json.simple.JSONObject;
import org.springframework.dao.DataAccessException;

import java.io.FileWriter;
import java.io.IOException;
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

public class StreamingCsvResultSetExtractor extends AbsResultSetExtractor<Void> {

    public StreamingCsvResultSetExtractor(UUID jobId) {
        super(jobId);
    }


    @Override
    public Void extractData(final ResultSet rs) throws SQLException, DataAccessException {
        createDirectories(prefix);
        try (
                var dataWriter = new CSVWriter(new FileWriter(dataPath.getPath()));
                var schemaWriter = new FileWriter(schemaPath.getPath());
        ) {
            var resultSetMetadata = rs.getMetaData();
            var columnCount = resultSetMetadata.getColumnCount();
            var schemaJson = new JSONObject();
            for (int i = 0; i < columnCount; i++) {
                var columnName = resultSetMetadata.getColumnName(i + 1);
                var columnType = Functions.getDolphinType(resultSetMetadata.getColumnType(i + 1));
                schemaJson.put(columnName, columnType.value());
            }
            schemaWriter.write(schemaJson.toJSONString());
            dataWriter.writeAll(rs, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
