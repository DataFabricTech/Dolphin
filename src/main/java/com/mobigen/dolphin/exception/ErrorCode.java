package com.mobigen.dolphin.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@AllArgsConstructor
@Getter
public enum ErrorCode {
    VALIDATION_ERROR(10000, "입력 값을 수정 해주세요: "),
    NON_EXISTENT_MODEL(10100, "모델을 찾을 수 없습니다: "),
    INVALID_SQL(20000, "잘 못 된 SQL 입니다: "),
    INVALID_SQL_DUPLICATED_COLUMNS(20001, "중복된 컬럼이 있습니다: "),
    EXECUTION_FAILED(20100, "쿼리 실행이 실패 했습니다: "),
    UNSUPPORTED(30000, "지원 하지 않는 동작 입니다: "),
    INTERNAL_ERROR(99999, "처리 하지 못 한 에러가 발생 했습니다: ");
    private final int status;
    private final String message;
}
