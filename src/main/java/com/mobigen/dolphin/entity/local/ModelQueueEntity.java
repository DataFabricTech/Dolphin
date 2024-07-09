package com.mobigen.dolphin.entity.local;

import jakarta.persistence.*;
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
    @GeneratedValue
    private Long id;
    private String trinoModelName;
    private String modelNameFqn;
    private String fromFqn;
    private Command command;

    public enum Command {
        LINEAGE,
        INGESTION,
    }
}
