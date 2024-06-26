package com.mobigen.dolphin.util;

import com.mobigen.dolphin.antlr.ModelSqlParser;
import org.antlr.v4.runtime.VocabularyImpl;

import java.sql.Types;
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class Functions {
    public static String getCatalogName(UUID id) {
        return "catalog_" + id.toString().replace("-", "_");
    }

    public static String convertKeywordName(String name) {
        Character specialChar = '"';
        if (name.startsWith("`")) {
            name = specialChar + name.substring(1, name.length() - 1) + specialChar;
        } else if (Arrays.asList(((VocabularyImpl) ModelSqlParser.VOCABULARY).getSymbolicNames())
                .contains("K_" + name.toUpperCase())) {
            name = specialChar + name + specialChar;
        } else if (!Pattern.matches("^[a-zA-Z_][a-zA-Z0-9_]*$", name)) {
            name = specialChar + name + specialChar;
        }
        return name;
    }

    public static DolphinType getDolphinType(int colType) {
        if (Types.BIT == colType) {
            return DolphinType.BOOL;
        } else if (Types.TINYINT == colType || Types.SMALLINT == colType || Types.INTEGER == colType) {
            return DolphinType.INT;
        } else if (Types.BIGINT == colType) {
            return DolphinType.LONG;
        } else if (Types.FLOAT == colType || Types.DOUBLE == colType || Types.REAL == colType || Types.NUMERIC == colType || Types.DECIMAL == colType) {
            return DolphinType.REAL;
        } else {
            return DolphinType.TEXT;
        }
    }

    public static Object convertType(String value, DolphinType type) {
        return value.isEmpty()
                ? null
                : switch (type) {
            case INT -> Integer.parseInt(value);
            case LONG -> Long.parseLong(value);
            case BOOL -> Boolean.parseBoolean(value);
            case REAL -> Double.parseDouble(value);
            default -> value;
        };
    }
}
