package com.mobigen.dolphin.dto.response;

import com.mobigen.dolphin.util.DolphinType;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Data
@Builder
public class QueryResultDto {
    private UUID jobId;
    private List<Column> columns;
    private ResultData resultData;
    private int totalRows;
    private int totalPages;
    private int page;
    private int size;

    @Data
    @Builder
    public static class Column {
        private String name;
        private DolphinType dataType;
        private String comment;
    }

    @Data
    @Builder
    public static class ResultData {
        private List<String> columns;
        private List<List<Object>> rows;
    }

    /**
     * This class is needed.
     * Do not remove this class !!!
     * Intellij can not recognize this is useful.
     */
    public static class QueryResultDtoBuilder {
        public QueryResultDtoBuilder resultData(ResultData resultData) {
            this.resultData = resultData;
            this.size = resultData.rows.size();
            return this;
        }
    }
}
