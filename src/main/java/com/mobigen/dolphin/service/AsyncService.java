package com.mobigen.dolphin.service;

import com.mobigen.dolphin.entity.local.JobEntity;
import com.mobigen.dolphin.repository.local.JobRepository;
import com.mobigen.dolphin.repository.trino.TrinoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
public class AsyncService {
    private final TrinoRepository trinoRepository;
    private final JobRepository jobRepository;

    @Async
    public void executeAsync(JobEntity jobEntity) {
        log.info("Run async job {}", jobEntity.getId());
        jobEntity.setStatus(JobEntity.JobStatus.RUNNING);
        jobRepository.save(jobEntity);
        var result = trinoRepository.asyncExecuteQuery(jobEntity.getId(), jobEntity.getConvertedQuery());
        jobEntity.setResultPath(result);
        jobEntity.setStatus(JobEntity.JobStatus.FINISHED);
        jobRepository.save(jobEntity);
    }
}
