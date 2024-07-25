package com.mobigen.dolphin.util;

import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
class FunctionsTest {
    @Test
    public void testGetCatalogName() {
        var randomId = UUID.randomUUID();
        var expected = "catalog_" + randomId.toString().replaceAll("-", "_");
        assertEquals(expected, Functions.getCatalogName(randomId));
    }

    @Test
    public void testConvertKeywordName() {
        assertEquals("\"from\"", Functions.convertKeywordName("from"));
        assertEquals("foo", Functions.convertKeywordName("foo"));
        assertEquals("\"foo\"", Functions.convertKeywordName("`foo`"));
    }

    @Test
    public void testGetDolphinType() {
        assertEquals(DolphinType.BOOL, Functions.getDolphinType(Types.BIT));
        assertEquals(DolphinType.INTEGER, Functions.getDolphinType(Types.TINYINT));
        assertEquals(DolphinType.INTEGER, Functions.getDolphinType(Types.SMALLINT));
        assertEquals(DolphinType.INTEGER, Functions.getDolphinType(Types.INTEGER));
        assertEquals(DolphinType.BIGINT, Functions.getDolphinType(Types.BIGINT));
        assertEquals(DolphinType.REAL, Functions.getDolphinType(Types.FLOAT));
        assertEquals(DolphinType.REAL, Functions.getDolphinType(Types.DOUBLE));
        assertEquals(DolphinType.REAL, Functions.getDolphinType(Types.REAL));
        assertEquals(DolphinType.REAL, Functions.getDolphinType(Types.NUMERIC));
        assertEquals(DolphinType.REAL, Functions.getDolphinType(Types.DECIMAL));
        assertEquals(DolphinType.TEXT, Functions.getDolphinType(Types.VARCHAR));
    }

    @Test
    public void testConvertType() {
        assertEquals(10, Functions.convertType("10", DolphinType.INTEGER));
        assertEquals(10L, Functions.convertType("10", DolphinType.BIGINT));
        assertEquals(10.0, Functions.convertType("10", DolphinType.REAL));
        assertEquals(true, Functions.convertType("true", DolphinType.BOOL));
        assertEquals("test", Functions.convertType("test", DolphinType.TEXT));
    }
}