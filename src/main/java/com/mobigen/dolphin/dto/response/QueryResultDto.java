package com.mobigen.dolphin.dto.response;

import com.mobigen.dolphin.util.DolphinType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Null;
import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    private long totalRows;
    private int totalPages;
    private int page;
    private int size;
    @Schema(description = "query executing time")
    private ZonedDateTime startedTime;
    @Schema(description = "query executing time")
    private Long startedUnixTimestamp;
    @Schema(description = "query finishing time")
    private ZonedDateTime finishedTime;
    @Schema(description = "query finishing time")
    private Long finishedUnixTimestamp;
    @Schema(description = "milli-seconds")
    private Double elapsedTime;

    public QueryResultDto(UUID jobId, List<Column> columns, ResultData resultData, long totalRows, int totalPages, int page, int size, ZonedDateTime startedTime, Long startedUnixTimestamp, ZonedDateTime finishedTime, Long finishedUnixTimestamp, Double elapsedTime) {
        this.jobId = jobId;
        this.columns = columns;
        this.resultData = resultData;
        this.totalRows = totalRows;
        this.totalPages = totalPages;
        this.page = page;
        this.size = size;
        if (startedTime == null) {
            startedTime = ZonedDateTime.now(ZoneId.systemDefault());
        }
        this.startedTime = startedTime;
        this.startedUnixTimestamp = startedUnixTimestamp;
        if (finishedTime == null) {
            finishedTime = ZonedDateTime.now(ZoneId.systemDefault());
        }
        this.finishedTime = finishedTime;
        this.finishedUnixTimestamp = finishedUnixTimestamp;
        if (elapsedTime == null) {
            elapsedTime = Duration.between(startedTime, finishedTime).toNanos() / 1000000.0;
        }
        this.elapsedTime = elapsedTime;
    }

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

        public QueryResultDtoBuilder startedTime(LocalDateTime startedTime) {
            this.startedTime = ZonedDateTime.of(startedTime, ZoneId.systemDefault());
            this.startedUnixTimestamp = this.startedTime.toInstant().toEpochMilli();
            return this;
        }

        public QueryResultDtoBuilder finishedTime(LocalDateTime finishedTime) {
            this.finishedTime = ZonedDateTime.of(finishedTime, ZoneId.systemDefault());
            this.finishedUnixTimestamp = this.finishedTime.toInstant().toEpochMilli();
            return this;
        }
    }
}
