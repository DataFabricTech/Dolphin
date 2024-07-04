package com.mobigen.dolphin.util;

import com.mobigen.dolphin.dto.response.QueryResultDto;
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
        var result = CsvDeSerializer.readCsv(path.getPath(), 0, 7);
        assertEquals(7, result.getTotalCount());
        assertEquals(List.of(
                QueryResultDto.Column.builder().name("product").dataType(DolphinType.TEXT).build(),
                QueryResultDto.Column.builder().name("series").dataType(DolphinType.TEXT).build(),
                QueryResultDto.Column.builder().name("sales_price").dataType(DolphinType.INTEGER).build()
        ), result.getColumns());
        assertEquals(List.of("GTX Basic", "GTX", 550), result.getResultData().getRows().getFirst());
    }
}