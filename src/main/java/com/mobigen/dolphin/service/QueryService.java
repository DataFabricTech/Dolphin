package com.mobigen.dolphin.service;

import com.mobigen.dolphin.antlr.ModelSqlLexer;
import com.mobigen.dolphin.antlr.ModelSqlParser;
import com.mobigen.dolphin.antlr.ModelSqlParsingVisitor;
import com.mobigen.dolphin.config.DolphinConfiguration;
import com.mobigen.dolphin.dto.request.ExecuteDto;
import com.mobigen.dolphin.dto.response.QueryResultDTO;
import com.mobigen.dolphin.entity.local.JobEntity;
import com.mobigen.dolphin.exception.SqlParseException;
import com.mobigen.dolphin.repository.local.FusionModelRepository;
import com.mobigen.dolphin.repository.local.JobRepository;
import com.mobigen.dolphin.repository.openmetadata.OpenMetadataRepository;
import com.mobigen.dolphin.repository.trino.TrinoRepository;
import com.mobigen.dolphin.util.CsvDeSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.stereotype.Service;

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
@Service
public class QueryService {
    private final OpenMetadataRepository openMetadataRepository;
    private final DolphinConfiguration dolphinConfiguration;
    private final TrinoRepository trinoRepository;
    private final JobRepository jobRepository;
    private final FusionModelRepository fusionModelRepository;

    private final AsyncService asyncService;

    public JobEntity createJob(ExecuteDto executeDto) {
        var lexer = new ModelSqlLexer(CharStreams.fromString(executeDto.getQuery()));
        var tokens = new CommonTokenStream(lexer);
        var parser = new ModelSqlParser(tokens);
        log.info("Create job. origin sql: {}", executeDto.getQuery());
        var job = JobEntity.builder()
                .status(JobEntity.JobStatus.INIT)
                .userQuery(executeDto.getQuery())
                .build();
        jobRepository.save(job);
        var visitor = new ModelSqlParsingVisitor(job, openMetadataRepository, dolphinConfiguration, executeDto.getReferenceModels());
        var parseTree = parser.parse();
        var convertedQuery = visitor.visit(parseTree);
        log.info("Converted sql: {}", convertedQuery);
        job.setStatus(JobEntity.JobStatus.QUEUED);
        job.setConvertedQuery(convertedQuery);
        jobRepository.save(job);
        fusionModelRepository.saveAll(visitor.getUsedModelHistory());
        return job;
    }

    public QueryResultDTO execute(ExecuteDto executeDto) {
        var job = createJob(executeDto);
        job.setStatus(JobEntity.JobStatus.RUNNING);
        jobRepository.save(job);
        try {
            var result = trinoRepository.executeQuery2(job.getConvertedQuery());
            job.setStatus(JobEntity.JobStatus.FINISHED);
            jobRepository.save(job);
            return result;
        } catch (SqlParseException e) {
            job.setStatus(JobEntity.JobStatus.FAILED);
            jobRepository.save(job);
            throw e;
        }
    }

    public QueryResultDTO executeAsync(ExecuteDto executeDto) {
        var job = createJob(executeDto);
        asyncService.executeAsync(job);
        return QueryResultDTO.builder()
                .jobId(job.getId())
                .build();
    }

    public Object status(UUID jobId) {
        var job = jobRepository.findById(jobId);
        if (job.isEmpty()) {
            throw new RuntimeException("job not found");
        }
        return job.get().getStatus();
    }

    public QueryResultDTO read(UUID jobId) {
        var job = jobRepository.findById(jobId);
        if (job.isEmpty()) {
            throw new RuntimeException("job not found");
        }
        return CsvDeSerializer.readCsv(job.get().getResultPath());
    }
}
