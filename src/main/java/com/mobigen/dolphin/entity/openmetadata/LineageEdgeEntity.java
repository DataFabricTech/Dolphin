package com.mobigen.dolphin.entity.openmetadata;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Getter
@Setter
@Builder
public class LineageEdgeEntity {
    private String description;
    private OMBaseEntity fromEntity;
    private LineageDetails lineageDetails;
    private OMBaseEntity toEntity;

    public static class LineageDetails {
        private List<ColumnLineage> columnLineage;
        private String description;
        private OMBaseEntity pipeline;
        private Source source;
        private String sqlQuery;

        public static class ColumnLineage {
            private List<String> fromColumns;
            private String function;
            private String toColumn;
        }

        public enum Source {
            Manual,
            ViewLineage,
            QueryLineage,
            PipelineLineage,
            DashboardLineage,
            DbtLineage,
            SparkLineage,
            OpenLineage,
            External,
            TableLineage,
        }
    }
}
