package com.mobigen.dolphin.util;

import com.mobigen.dolphin.antlr.ModelSqlLexer;
import com.mobigen.dolphin.antlr.ModelSqlParser;
import com.mobigen.dolphin.antlr.SqlErrorListener;
import com.mobigen.dolphin.antlr.SqlVisitor;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
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
            return DolphinType.INTEGER;
        } else if (Types.BIGINT == colType) {
            return DolphinType.BIGINT;
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
            case INTEGER -> Integer.parseInt(value);
            case BIGINT -> Long.parseLong(value);
            case BOOL -> Boolean.parseBoolean(value);
            case REAL -> Double.parseDouble(value);
            default -> value;
        };
    }

    public static ModelSqlParser.ParseContext getParseTree(String sql) {
        var lexer = new ModelSqlLexer(CharStreams.fromString(sql));
        var tokens = new CommonTokenStream(lexer);
        var parser = new ModelSqlParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(SqlErrorListener.INSTANCE);
        return parser.parse();
    }

    public static String getConvertedSql(SqlVisitor visitor, ModelSqlParser.ParseContext parseTree) {
        return visitor.visit(parseTree);
    }
}
