package com.mobigen.dolphin.entity.local;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "fusion_model")
public class FusionModelEntity {
    @Id
    @GeneratedValue
    private Long id;
    private UUID modelIdOfOM;
    private String fullyQualifiedName;
    private String trinoModelName;
    @ManyToOne  // Many=fusionModel, One=job
    @JoinColumn(name = "job_id")
    private JobEntity job;

}
