package com.mobigen.dolphin.entity.local;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "model_queue")
public class ModelQueueEntity {
    @Id
    @Column(name = "trino_model_name", unique = true, nullable = false)
    private String trinoModelName;
    private String modelNameFqn;
    private String fromFqn;
    private Command command;

    public enum Command {
        LINEAGE,
        INGESTION,
    }
}
