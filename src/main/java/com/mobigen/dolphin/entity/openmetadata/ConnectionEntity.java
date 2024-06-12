package com.mobigen.dolphin.entity.openmetadata;

import lombok.Data;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */

@Data
public class ConnectionEntity {
    private ConfigEntity config;

    @Data
    public static class ConfigEntity {
        private String type;
        private String scheme;
        private String username;
        private String password;
        private AuthType authType;
        private String hostPort;
        private String database;
        private AwsConfigEntity awsConfig;
        private Boolean supportsMetadataExtraction;
        private Boolean supportsDBTExtraction;
        private Boolean supportsProfiler;
        private Boolean supportsQueryComment;
    }

    @Data
    public static class AuthType {
        private String password;
    }

    @Data
    public static class AwsConfigEntity {
        private String awsAccessKeyId;
        private String awsSecretAccessKey;
        private String awsRegion;
        private String endPointURL;
        private String assumeRoleSessionName;
    }
}
