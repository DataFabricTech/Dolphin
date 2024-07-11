package com.mobigen.dolphin.service;

import com.mobigen.dolphin.entity.openmetadata.EntityType;
import com.mobigen.dolphin.entity.openmetadata.LineageEdgeEntity;
import com.mobigen.dolphin.entity.openmetadata.OMBaseEntity;
import com.mobigen.dolphin.repository.local.ModelQueueRepository;
import com.mobigen.dolphin.repository.openmetadata.OpenMetadataRepository;
import com.mobigen.dolphin.util.IngestionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
@RequiredArgsConstructor
@EnableScheduling
@Component
public class ModelQueueScheduler {
    private final ModelQueueRepository modelQueueRepository;
    private final OpenMetadataRepository openMetadataRepository;

    @Scheduled(fixedRate = 5000)
    public void modelQueueJob() {
        var data = modelQueueRepository.findAll();
        if (data.isEmpty()) {
            return;
        }
        int i = 0;
        for (var modelQueue : data) {
            OMBaseEntity fromTable;
            OMBaseEntity toTable;
            try {
                fromTable = openMetadataRepository.getTable(modelQueue.getFromFqn(), OMBaseEntity.class);
                fromTable.setType(EntityType.TABLE);
                toTable = openMetadataRepository.getTable(modelQueue.getModelNameFqn(), OMBaseEntity.class);
                toTable.setType(EntityType.TABLE);
                log.info("Start to add lineage from {} to {}", fromTable.getFullyQualifiedName(), toTable.getFullyQualifiedName());
                var lineageEdgeEntity = LineageEdgeEntity.builder()
                        .fromEntity(fromTable)
                        .toEntity(toTable)
                        .build();
                openMetadataRepository.addLineageEdge(lineageEdgeEntity);
                modelQueueRepository.delete(modelQueue);
                log.info("Succeed to add lineage from {} to {}", modelQueue.getFromFqn(), modelQueue.getModelNameFqn());
            } catch (Exception e) {
                log.error("Failed to add lineage from {} to {}", modelQueue.getFromFqn(), modelQueue.getModelNameFqn(), e);
            }
            i++;
        }
        if (i > 0) {
            // sample data 추가 등 profiling 을 위해 ingestion 을 호출
            openMetadataRepository.callIngestion(IngestionType.PROFILER);
        }
    }
}
