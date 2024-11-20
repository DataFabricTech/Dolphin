package com.mobigen.dolphin.entity.openmetadata;

import lombok.Data;

import java.util.List;

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
        private MinIOConfigEntity minioConfig;
        private List<String> bucketNames;
        private OracleConnectionTypeEntity oracleConnectionType;
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

    @Data
    public static class MinIOConfigEntity {
        private String accessKeyId;
        private String secretKey;
        private String endPointURL;
    }

    @Data
    public static class OracleConnectionTypeEntity {
        private String databaseSchema;
        private String oracleServiceName;
        private String oracleTNSConnection;
    }
}
