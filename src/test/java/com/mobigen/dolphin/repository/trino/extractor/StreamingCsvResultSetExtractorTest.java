package com.mobigen.dolphin.repository.trino.extractor;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.UUID;

import static org.mockito.Mockito.when;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
class StreamingCsvResultSetExtractorTest {

    @Mock
    private ResultSet resultSet;
    @Mock
    private ResultSetMetaData resultSetMetaData;

    @Test
    void testExtractData() throws SQLException {
        when(resultSetMetaData.getColumnCount()).thenReturn(3);
        when(resultSetMetaData.getColumnName(0)).thenReturn("product");
        when(resultSetMetaData.getColumnType(0)).thenReturn(12);
        when(resultSetMetaData.getColumnName(1)).thenReturn("series");
        when(resultSetMetaData.getColumnType(1)).thenReturn(12);
        when(resultSetMetaData.getColumnName(2)).thenReturn("sales_price");
        when(resultSetMetaData.getColumnType(2)).thenReturn(4);

        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSet.getString("product")).thenReturn("myHost");
        when(resultSet.getString("zos")).thenReturn("myOS");
        when(resultSet.getString("customer_name")).thenReturn("myCustomerName");

        var id = UUID.randomUUID();
        var extractor = new StreamingCsvResultSetExtractor(id);
        extractor.extractData(resultSet);
    }
}