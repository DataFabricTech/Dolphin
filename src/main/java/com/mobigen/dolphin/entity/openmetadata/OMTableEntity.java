package com.mobigen.dolphin.entity.openmetadata;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OMTableEntity extends OMBaseEntity {
    private OMChangeDescriptionEntity changeDescription;
    private List<OMBaseEntity> children;
    private List<OMColumn> columns;
    private List<OMCustomMetric> customMetrics;
    // private dataModel;
    private List<OMBaseEntity> dataProducts;
    private OMBaseEntity database;
    private OMBaseEntity databaseSchema;
    private OMBaseEntity service;
    private OMBaseEntity domain;
    private List<OMBaseEntity> experts;
    // private fileFormat;
    private List<OMBaseEntity> followers;
    // private joins;
    // private lifeCycle;
    private OMBaseEntity location;
    private OMBaseEntity owner;
    // private profile;
    // private provider
//    private String retentionPeriod;
    private List<OMBaseEntity> reviewers;
    private Float version;
    private Long updatedAt;
    private String updatedBy;
    private String tableType;
    private String serviceType;
    private String sourceHash;

    @Data
    public static class OMColumn {
        //        private String arrayDataType;
        private List<OMColumn> children;
        //        private String constraint;
        private List<OMCustomMetric> customMetrics;
        private Integer dataLength;
        private String dataType;
        private String dataTypeDisplay;
        private String jsonSchema;
        private Integer ordinalPosition;
        private Integer precision;
        //        private profile;
        private Integer scale;
//        private tags;
    }

    @Data
    public static class OMCustomMetric {
        private String columnName;
        private String description;
        private String expression;
        private UUID id;
        private String name;
        private OMBaseEntity owner;
        private Long updatedAt;
        private String updatedBy;
    }
}
