package com.mobigen.dolphin.util;

import com.mobigen.dolphin.dto.response.QueryResultDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
class CsvDeSerializerTest {

    @Test
    void testReadCsv() {
        var path = getClass().getClassLoader().getResource("data/result_data_1");
        assert path != null;
        var result = CsvDeSerializer.readCsv(path.getPath());
        assertEquals(7, result.getTotalCount());
        assertEquals(List.of(
                QueryResultDTO.Column.builder().name("product").type(DolphinType.TEXT).build(),
                QueryResultDTO.Column.builder().name("series").type(DolphinType.TEXT).build(),
                QueryResultDTO.Column.builder().name("sales_price").type(DolphinType.INTEGER).build()
        ), result.getColumns());
        assertEquals(List.of("GTX Basic", "GTX", 550), result.getRows().getFirst());
    }
}