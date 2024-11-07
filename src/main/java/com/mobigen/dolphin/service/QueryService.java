package com.mobigen.dolphin.service;

import com.mobigen.dolphin.antlr.SqlVisitor;
import com.mobigen.dolphin.antlr.SqlWithoutLimitVisitor;
import com.mobigen.dolphin.config.DolphinConfiguration;
import com.mobigen.dolphin.dto.request.ExecuteDto;
import com.mobigen.dolphin.dto.response.QueryResultDto;
import com.mobigen.dolphin.entity.local.JobEntity;
import com.mobigen.dolphin.exception.DolphinException;
import com.mobigen.dolphin.exception.ErrorCode;
import com.mobigen.dolphin.repository.MixRepository;
import com.mobigen.dolphin.repository.local.FusionModelRepository;
import com.mobigen.dolphin.repository.local.JobRepository;
import com.mobigen.dolphin.repository.openmetadata.OpenMetadataRepository;
import com.mobigen.dolphin.repository.trino.TrinoRepository;
import com.mobigen.dolphin.util.CsvDeSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.mobigen.dolphin.util.Functions.getConvertedSql;
import static com.mobigen.dolphin.util.Functions.getParseTree;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class QueryService {
    private final OpenMetadataRepository openMetadataRepository;
    private final DolphinConfiguration dolphinConfiguration;
    private final TrinoRepository trinoRepository;
    private final MixRepository mixRepository;
    private final JobRepository jobRepository;
    private final FusionModelRepository fusionModelRepository;

    private final AsyncService asyncService;


    public JobEntity createJob(ExecuteDto executeDto) {
        return createJob(executeDto, false);
    }

    public JobEntity createJob(ExecuteDto executeDto, boolean separateLimitation) {
        log.info("Parse query: {}", executeDto.getQuery());
        var parseTree = getParseTree(executeDto.getQuery());

        log.info("Create job. origin sql: {}", executeDto.getQuery());
        var job = JobEntity.builder()
                .status(JobEntity.JobStatus.INIT)
                .userQuery(executeDto.getQuery())
                .build();
        jobRepository.save(job);

        SqlVisitor visitor;
        if (separateLimitation) {
            log.info("Use limitation separator visitor");
            visitor = new SqlWithoutLimitVisitor(job, openMetadataRepository, mixRepository, dolphinConfiguration, executeDto.getReferenceModels());
        } else {
            log.info("Use origin visitor");
            visitor = new SqlVisitor(job, openMetadataRepository, mixRepository, dolphinConfiguration, executeDto.getReferenceModels());
        }
        var convertedQuery = getConvertedSql(visitor, parseTree);
        log.info("Converted sql: {}, separate limitation: {}", convertedQuery, separateLimitation);
        job.setStatus(JobEntity.JobStatus.QUEUED);
        job.setConvertedQuery(convertedQuery);
        if (separateLimitation) {
            var pagination = ((SqlWithoutLimitVisitor) visitor).getPagination();
            log.info("separated limitation: {}", pagination);
            if (pagination != null) {
                job.setOffset_(pagination.left());
                job.setLimit_(pagination.right());
            }
        }
        jobRepository.save(job);
        return job;
    }

    public QueryResultDto execute(ExecuteDto executeDto) {
        var job = createJob(executeDto, true);  // blocking 실행의 경우 limit, offset 을 sql 과 분리
        log.info("Run blocking job: {}", job.getId());
        job.setStatus(JobEntity.JobStatus.RUNNING);
        jobRepository.save(job);
        try {
            var result = trinoRepository.executeQuery(job.getConvertedQuery(),
                    job.getLimit_(), job.getOffset_(),
                    executeDto.getLimit(), executeDto.getPage());
            job.setStatus(JobEntity.JobStatus.FINISHED);
            jobRepository.save(job);
            return result;
        } catch (Exception e) {
            job.setStatus(JobEntity.JobStatus.FAILED);
            jobRepository.save(job);
            throw new DolphinException(ErrorCode.EXECUTION_FAILED, e.getMessage());
        }
    }

    public QueryResultDto executeAsync(ExecuteDto executeDto) {
        var job = createJob(executeDto);
        asyncService.executeAsync(job);
        return QueryResultDto.builder()
                .jobId(job.getId())
                .build();
    }

    public JobEntity.JobStatus status(UUID jobId) {
        log.info("Get status: {}", jobId);
        var job = jobRepository.findById(jobId);
        if (job.isEmpty()) {
            throw new RuntimeException("job not found");
        }
        return job.get().getStatus();
    }

    public QueryResultDto read(UUID jobId, Integer page, Integer limit) {
        log.info("Read job: {}, page: {}, limit: {}", jobId, page, limit);
        var job = jobRepository.findById(jobId);
        if (job.isEmpty()) {
            throw new RuntimeException("job not found");
        }
        return CsvDeSerializer.readCsv(job.get().getResultPath(), page, limit);
    }
}
