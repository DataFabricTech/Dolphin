package com.mobigen.dolphin.antlr;

import com.mobigen.dolphin.exception.ErrorCode;
import com.mobigen.dolphin.exception.SqlParseException;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class SqlErrorListener extends BaseErrorListener {
    public static final SqlErrorListener INSTANCE = new SqlErrorListener();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        throw new SqlParseException(ErrorCode.INVALID_SQL, "line " + line + ":" + charPositionInLine + ": " + msg);
    }
}
