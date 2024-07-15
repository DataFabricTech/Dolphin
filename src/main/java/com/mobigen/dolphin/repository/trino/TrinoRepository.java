package com.mobigen.dolphin.repository.trino;

import com.mobigen.dolphin.config.DolphinConfiguration;
import com.mobigen.dolphin.dto.response.ModelDto;
import com.mobigen.dolphin.dto.response.QueryResultDto;
import com.mobigen.dolphin.exception.ErrorCode;
import com.mobigen.dolphin.exception.SqlParseException;
import com.mobigen.dolphin.repository.trino.extractor.ExtractType;
import com.mobigen.dolphin.repository.trino.extractor.ResultSetExtractorFactory;
import com.mobigen.dolphin.util.Functions;
import jakarta.annotation.PostConstruct;
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

    @PostConstruct
    public void getOrCreateMetastoreCatalog() {
        var catalogs = getCatalogs();
        var catalogName = dolphinConfiguration.getModel().getCatalog();
        boolean makeCatalog = true;
        for (var catalog : catalogs) {
            if (catalog.equals(catalogName)) {
                log.info("Already created trino catalog {}", catalogName);
                makeCatalog = false;
                break;
            }
        }
        if (makeCatalog) {
            execute("create catalog " + catalogName
                    + " using hive"
                    + " with ("
                    + " \"hive.metastore.uri\" = '" + dolphinConfiguration.getHiveMetastore().getUri() + "'"
                    + ")");
        }
    }

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
        log.info("End of executing {}", sql);
    }

    public List<String> getCatalogs() {
        return trinoJdbcTemplate.query("show catalogs",
                (rs, rowNum) -> rs.getString("Catalog"));
    }

    public Long countResult(String sql) {
        var countSql = "select count(*) from (" + sql + ")";
        return trinoJdbcTemplate.queryForObject(countSql, Long.class);
    }

    private String addLimitOffset(String sql, int offset, int limit) {
        return sql + " offset " + offset + " limit " + limit;
    }

    public QueryResultDto executeQuery(String sql, Integer queryLimit, Integer queryOffset, Integer apiLimit, Integer apiPage) {
        Long totalRows;
        int totalPages;
        int page;
        if (queryLimit != null && queryOffset != null) {  // query 에 limit, offset 이 있는 경우, limit 된 결과의 total 계산
            sql = addLimitOffset(sql, queryOffset, queryLimit);
            totalRows = countResult(sql);
            totalPages = 1;
            page = 0;
        } else { // query 에 limit, offset 이 없는 경우, api 요청에 의한 결과기 때문에, 원본 sql 의 total 계산
            totalRows = countResult(sql);
            totalPages = (int) Math.ceil((double) totalRows / apiLimit);
            page = apiPage;
            var offset = apiPage * apiLimit;
            sql = addLimitOffset(sql, offset, apiLimit);
        }
        log.info("Executing {}", sql);
        // get model data
        List<QueryResultDto.Column> columns = new ArrayList<>();
        List<String> columnNames = new ArrayList<>();
        try {
            var rows = trinoJdbcTemplate.query(sql, ((rs, rowNum) -> {
                var rsmd = rs.getMetaData();
                int numberOfColumns = rsmd.getColumnCount();
                if (rowNum == 0) {
                    for (int i = 1; i <= numberOfColumns; i++) {
                        var name = rsmd.getColumnName(i);
                        columns.add(QueryResultDto.Column.builder()
                                .name(name)
                                .dataType(Functions.getDolphinType(rsmd.getColumnType(i)))
                                .build());
                        columnNames.add(name);
                    }
                }
                List<Object> row = new ArrayList<>();
                for (int i = 1; i <= numberOfColumns; i++) {
                    row.add(rs.getObject(i));
                }
                return row;
            }));
            log.info("End of executing {}", sql);
            return QueryResultDto.builder()
                    .columns(columns)
                    .resultData(QueryResultDto.ResultData.builder()
                            .columns(columnNames)
                            .rows(rows)
                            .build())
                    .totalRows(totalRows)
                    .totalPages(totalPages)
                    .page(page)
                    .build();
        } catch (UncategorizedSQLException e) {
            log.error(e.getMessage(), e);
            throw new SqlParseException(ErrorCode.INVALID_SQL, Objects.requireNonNull(e.getSQLException()).getCause().getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new SqlParseException(ErrorCode.INVALID_SQL, e.getMessage());
        }
    }


    public String asyncExecuteQuery(UUID jobId, String sql, Boolean direct) {
        log.info("Executing {}", sql);
        var totalRows = countResult(sql);
        if (!direct) {
            // 결과를 가져와서 파일로 저장
            var extractor = ResultSetExtractorFactory.createResultSetExtractor(ExtractType.CSV, jobId, totalRows);
            trinoJdbcTemplate.query(sql, extractor);
            log.info("End of executing {}", sql);
            return extractor.getPrefix();
        } else {
            // trino 가 직접 hive table 생성을 통해서 결과 데이터 저장
            var resultTableName = "internalhive.dolphin_cache.data_" + (jobId.hashCode() & 0xfffffff);
            var s = "create table " + resultTableName +
                    " with (format = 'PARQUET', external_location = 's3a://warehouse/result/" + jobId + "')" +
                    " as " + sql;
            trinoJdbcTemplate.execute(s);
            log.info("End of executing {}", sql);
            return resultTableName;
        }
    }

    public String asyncExecuteQuery(UUID jobId, String sql) {
        return asyncExecuteQuery(jobId, sql, false);
    }


}
