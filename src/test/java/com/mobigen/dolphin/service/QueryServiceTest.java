package com.mobigen.dolphin.service;

import com.mobigen.dolphin.config.DolphinConfiguration;
import com.mobigen.dolphin.dto.request.ExecuteDto;
import com.mobigen.dolphin.dto.response.QueryResultDto;
import com.mobigen.dolphin.entity.local.JobEntity;
import com.mobigen.dolphin.entity.openmetadata.OMBaseEntity;
import com.mobigen.dolphin.entity.openmetadata.OMDBServiceEntity;
import com.mobigen.dolphin.entity.openmetadata.OMTableEntity;
import com.mobigen.dolphin.repository.local.FusionModelRepository;
import com.mobigen.dolphin.repository.local.JobRepository;
import com.mobigen.dolphin.repository.openmetadata.OpenMetadataRepository;
import com.mobigen.dolphin.repository.trino.TrinoRepository;
import com.mobigen.dolphin.util.DolphinType;
import com.mobigen.dolphin.util.TrinoInit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
    private DolphinConfiguration dolphinConfiguration;
    @Mock
    private TrinoRepository trinoRepository;
    @Mock
    private OpenMetadataRepository openMetadataRepository;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private FusionModelRepository fusionModelRepository;
    @Mock
    private AsyncService asyncService;
    @MockBean
    private TrinoInit trinoInit;
    @InjectMocks
    private QueryService queryService;

    @BeforeEach
    public void beforeEach() {
        doReturn(Optional.ofNullable(JobEntity.builder()
                .status(JobEntity.JobStatus.RUNNING)
                .resultPath(Objects.requireNonNull(getClass().getClassLoader()
                                .getResource("data/result_data_1"))
                        .getPath()
                )
                .build()))
                .when(jobRepository).findById(any());
        doReturn(List.of("ok")).when(trinoRepository).getCatalogs();
        doNothing().when(trinoRepository).execute(any());
        doReturn(QueryResultDto.builder().build()).when(trinoRepository).executeQuery(any(), any(), any(), any(), any());
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
        doReturn(exampleTable).when(openMetadataRepository).getTable((UUID) any());
        doReturn(exampleTable).when(openMetadataRepository).getTable(anyString());
        doReturn(exampleTable).when(openMetadataRepository).getTable(anyString(), any());
        doAnswer(x -> {
            var schema = new DolphinConfiguration.ModelSchema();
            schema.setDb("default");
            var model = new DolphinConfiguration.Model();
            model.setCatalog("internalhive");
            model.setSchema(schema);
            return model;
        }).when(dolphinConfiguration).getModel();
        doReturn(null).when(fusionModelRepository).saveAll(any());
        doNothing().when(asyncService).executeAsync(any());
    }

    @Test
    public void testCreateJobQuery1() {
        var input = new ExecuteDto();
        input.setQuery("select a.id a_id, a.name as a_name from abc a");
        var job = queryService.createJob(input);
        var expected = JobEntity.builder()
                .status(JobEntity.JobStatus.QUEUED)
                .userQuery(input.getQuery())
                .convertedQuery("select a.id as a_id, a.name as a_name" +
                        " from internalhive.default.abc as a")
                .build();
        assertEquals(expected, job);
    }

    @Test
    public void testCreateJobQuery2() {
        var input = new ExecuteDto();
        input.setQuery("select a.id a_id, a.name as a_name, b.long, b.short from abc a, bcd b on a.id = b.id");
        var job = queryService.createJob(input, true);
        var expected = JobEntity.builder()
                .status(JobEntity.JobStatus.QUEUED)
                .userQuery(input.getQuery())
                .convertedQuery("select a.id as a_id, a.name as a_name, b.long, b.short" +
                        " from internalhive.default.abc as a ," +
                        " internalhive.default.bcd as b" +
                        " on a.id = b.id")
                .build();
        assertEquals(expected, job);
    }

    @Test
    public void testCreateJobQuery3() {
        var input = new ExecuteDto();
        input.setQuery("select a.id, a.name from abc a" +
                " union select b.id, b.name from bcd b limit 3,10");
        var job = queryService.createJob(input);
        var expected = JobEntity.builder()
                .status(JobEntity.JobStatus.QUEUED)
                .userQuery(input.getQuery())
                .convertedQuery("select a.id, a.name" +
                        " from internalhive.default.abc as a" +
                        " union select b.id, b.name" +
                        " from internalhive.default.bcd as b" +
                        " offset 3 limit 10")
                .build();
        assertEquals(expected, job);
    }

    @Test
    public void testCreateJobQuery4() {
        var input = new ExecuteDto();
        input.setQuery("select a.id, a.name from abc a" +
                " union select b.id, b.name from bcd b limit 3,10");
        var job = queryService.createJob(input, true);
        var expected = JobEntity.builder()
                .status(JobEntity.JobStatus.QUEUED)
                .userQuery(input.getQuery())
                .convertedQuery("select a.id, a.name" +
                        " from internalhive.default.abc as a" +
                        " union select b.id, b.name" +
                        " from internalhive.default.bcd as b")
                .limit_(10)
                .offset_(3)
                .build();
        assertEquals(expected, job);
    }

    @Test
    public void testExecute() {
        var input = new ExecuteDto();
        input.setQuery("select a.id, a.name from abc a" +
                " union select b.id, b.name from bcd b limit 3,10");
        input.setLimit(100);
        input.setPage(0);
        queryService.execute(input);
    }

    @Test
    public void testExecuteAsync() {
        var input = new ExecuteDto();
        input.setQuery("select a.id, a.name from abc a" +
                " union select b.id, b.name from bcd b limit 3,10");
        queryService.executeAsync(input);
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

        // 쿼리 시간은 동작 테스트와 상관 없으므로 맞춰준다.
        expected.setStartedTime(result.getStartedTime());
        expected.setFinishedTime(result.getFinishedTime());
        expected.setElapsedTime(result.getElapsedTime());
        assertEquals(expected, result);
    }
}