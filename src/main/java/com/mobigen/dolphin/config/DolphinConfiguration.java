package com.mobigen.dolphin.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static com.mobigen.dolphin.util.Functions.convertKeywordName;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Getter
@Setter
@ConfigurationPropertiesScan
@ConfigurationProperties(prefix = "dolphin")
@Configuration
public class DolphinConfiguration {
    private Model model;
    private OpenMetadataConfig openMetadata;
    private HiveMetastoreConfig hiveMetastore;

    @Getter
    @Setter
    public static class Model {
        private String prefix;
        private String catalog;
        private ModelSchema schema;
        private Character specialChar = '"';
        private String omTrinoDatabaseService;

        public String getCatalog() {
            return convertKeywordName(catalog);
        }
    }

    @Getter
    @Setter
    public static class ModelSchema {
        private String db;
        private String file;
    }

    @Getter
    @Setter
    public static class OpenMetadataConfig {
        private String apiUrl;
        private String botToken;
        private IngestionKey ingestion;

        @Getter
        @Setter
        public static class IngestionKey {
            private String metadata;
            private String profiler;
        }
    }


    @Getter
    @Setter
    public static class HiveMetastoreConfig {
        private String uri;
        private Map<String, String> options;
    }
}
