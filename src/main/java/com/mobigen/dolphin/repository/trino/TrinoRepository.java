package com.mobigen.dolphin.repository.trino;

import com.mobigen.dolphin.config.DolphinConfiguration;
import com.mobigen.dolphin.dto.response.ModelDto;
import com.mobigen.dolphin.dto.response.QueryResultDTO;
import com.mobigen.dolphin.exception.ErrorCode;
import com.mobigen.dolphin.exception.SqlParseException;
import com.mobigen.dolphin.repository.trino.extractor.ExtractType;
import com.mobigen.dolphin.repository.trino.extractor.ResultSetExtractorFactory;
import com.mobigen.dolphin.util.Functions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
@RequiredArgsConstructor
@Repository
public class TrinoRepository {
    private final DolphinConfiguration dolphinConfiguration;
    private final JdbcTemplate trinoJdbcTemplate;

    public List<ModelDto> getModelList() {
        // SHOW TABLES [ FROM schema ] [ LIKE pattern ]
        return trinoJdbcTemplate.query("show tables from " +
                        dolphinConfiguration.getModel().getCatalog()
                        + "." + dolphinConfiguration.getModel().getSchema().getDb(),
                (rs, rowNum) -> ModelDto.builder()
                        .name(rs.getString("Table"))
                        .build());
    }

    public void execute(String sql) {
        log.info("Executing {}", sql);
        trinoJdbcTemplate.execute(sql);
    }

    public List<String> getCatalogs() {
        return trinoJdbcTemplate.query("show catalogs",
                (rs, rowNum) -> rs.getString("Catalog"));
    }

    public QueryResultDTO executeQuery2(String sql) {
        // get model data
        List<QueryResultDTO.Column> columns = new ArrayList<>();
        try {
            var rows = trinoJdbcTemplate.query(sql, ((rs, rowNum) -> {
                var rsmd = rs.getMetaData();
                int numberOfColumns = rsmd.getColumnCount();
                if (rowNum == 0) {
                    for (int i = 1; i <= numberOfColumns; i++) {
                        columns.add(QueryResultDTO.Column.builder()
                                .name(rsmd.getColumnName(i))
                                .type(Functions.getDolphinType(rsmd.getColumnType(i)))
                                .build());
                    }
                }
                List<Object> row = new ArrayList<>();
                for (int i = 1; i <= numberOfColumns; i++) {
                    row.add(rs.getObject(i));
                }
                return row;
            }));
            return QueryResultDTO.builder()
                    .columns(columns)
                    .rows(rows)
                    .build();
        } catch (UncategorizedSQLException e) {
            log.error(e.getMessage(), e);
            throw new SqlParseException(ErrorCode.INVALID_SQL, Objects.requireNonNull(e.getSQLException()).getCause().getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new SqlParseException(ErrorCode.INVALID_SQL, e.getMessage());
        }
    }


    public String executeQuery(UUID jobId, String sql, Boolean direct) {
        if (!direct) {
            // 결과를 가져와서 파일로 저장
            var extractor = ResultSetExtractorFactory.createResultSetExtractor(ExtractType.CSV, jobId);
            trinoJdbcTemplate.query(sql, extractor);
            return extractor.getPrefix();
        } else {
            // trino 가 직접 hive table 생성을 통해서 결과 데이터 저장
            var resultTableName = "internalhive.dolphin_cache.data_" + (jobId.hashCode() & 0xfffffff);
            var s = "create table " + resultTableName +
                    " with (format = 'PARQUET', external_location = 's3a://warehouse/result/" + jobId + "')" +
                    " as " + sql;
            trinoJdbcTemplate.execute(s);
            return resultTableName;
        }
    }

    public String executeQuery(UUID jobId, String sql) {
        return executeQuery(jobId, sql, false);
    }


}
