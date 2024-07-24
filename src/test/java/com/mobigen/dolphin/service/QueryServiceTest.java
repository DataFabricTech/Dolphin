package com.mobigen.dolphin.service;

import com.mobigen.dolphin.dto.request.ExecuteDto;
import com.mobigen.dolphin.dto.response.QueryResultDto;
import com.mobigen.dolphin.entity.local.JobEntity;
import com.mobigen.dolphin.entity.openmetadata.OMBaseEntity;
import com.mobigen.dolphin.entity.openmetadata.OMDBServiceEntity;
import com.mobigen.dolphin.entity.openmetadata.OMTableEntity;
import com.mobigen.dolphin.repository.local.JobRepository;
import com.mobigen.dolphin.repository.openmetadata.OpenMetadataRepository;
import com.mobigen.dolphin.repository.trino.TrinoRepository;
import com.mobigen.dolphin.util.DolphinType;
import com.mobigen.dolphin.util.TrinoInit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@ActiveProfiles("test")
@SpringBootTest
@TestConfiguration
class QueryServiceTest {
    @Mock
    private TrinoRepository trinoRepository;
    @Mock
    private OpenMetadataRepository openMetadataRepository;
    @Mock
    private JobRepository jobRepository;
    @MockBean
    private TrinoInit trinoInit;
    @InjectMocks
    @Autowired
    private QueryService queryService;

    @BeforeEach
    public void beforeEach() {
        doAnswer(x -> Optional.ofNullable(JobEntity.builder()
                .status(JobEntity.JobStatus.RUNNING)
                .resultPath(Objects.requireNonNull(getClass().getClassLoader()
                                .getResource("data/result_data_1"))
                        .getPath()
                )
                .build()))
                .when(jobRepository).findById(any());
        doAnswer(x -> List.of("ok")).when(trinoRepository).getCatalogs();
        doNothing().when(trinoRepository).execute(any());
        var exampleTable = OMTableEntity.builder()
                .name("result_data_1")
                .service(OMBaseEntity.builder()
                        .id(UUID.randomUUID())
                        .build())
                .serviceType("postgres")
                .database(OMDBServiceEntity.builder()
                        .name("public")
                        .build())
                .build();
        doAnswer(x -> exampleTable).when(openMetadataRepository).getTable((UUID) any());
        doAnswer(x -> exampleTable).when(openMetadataRepository).getTable(anyString());
        doAnswer(x -> exampleTable).when(openMetadataRepository).getTable(anyString(), any());
    }

    @Test
    public void testCreateJob() {
        var input = new ExecuteDto();
        input.setQuery("select * from abc a");
        var job = queryService.createJob(input);
        var expected = JobEntity.builder()
                .status(JobEntity.JobStatus.QUEUED)
                .userQuery(input.getQuery())
                .convertedQuery("select a.id from internalhive.default.abc a")
                .build();
        assertEquals(expected, job);
    }

    @Test
    public void testStatus() {
        var status = queryService.status(UUID.randomUUID());
        assertEquals(JobEntity.JobStatus.RUNNING, status);
    }

    @Test
    public void testRead() {
        var result = queryService.read(UUID.randomUUID(), 0, 2);
        var row1 = new ArrayList<>();
        row1.add("GTX Basic");
        row1.add("GTX");
        row1.add(550);
        var row2 = new ArrayList<>();
        row2.add("GTX Pro");
        row2.add("GTX");
        row2.add(4821);

        var expected = QueryResultDto.builder()
                .columns(List.of(
                        QueryResultDto.Column.builder()
                                .name("product")
                                .dataType(DolphinType.TEXT)
                                .build(),
                        QueryResultDto.Column.builder()
                                .name("series")
                                .dataType(DolphinType.TEXT)
                                .build(),
                        QueryResultDto.Column.builder()
                                .name("sales_price")
                                .dataType(DolphinType.INTEGER)
                                .build()
                ))
                .resultData(QueryResultDto.ResultData.builder()
                        .columns(List.of("product", "series", "sales_price"))
                        .rows(List.of(row1, row2))
                        .build())
                .totalRows(7)
                .totalPages(4)
                .build();
        assertEquals(expected, result);
    }
}