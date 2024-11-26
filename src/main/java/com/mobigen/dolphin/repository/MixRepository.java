package com.mobigen.dolphin.repository;

import com.mobigen.dolphin.config.DolphinConfiguration;
import com.mobigen.dolphin.entity.openmetadata.OMBaseEntity;
import com.mobigen.dolphin.entity.openmetadata.OMServiceEntity;
import com.mobigen.dolphin.repository.openmetadata.OpenMetadataRepository;
import com.mobigen.dolphin.repository.trino.TrinoRepository;
import com.mobigen.dolphin.util.Functions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class MixRepository {
    private final DolphinConfiguration dolphinConfiguration;
    private final TrinoRepository trinoRepository;
    private final OpenMetadataRepository openMetadataRepository;

    public String getOrCreateTrinoCatalog(OMBaseEntity omBaseEntity) {
        var connInfo = openMetadataRepository.getConnectorInfo(omBaseEntity.getId(), omBaseEntity.getType());
        return getOrCreateTrinoCatalog(connInfo);
    }

    public String getOrCreateTrinoCatalog(OMServiceEntity omServiceEntity) {
        var catalogs = trinoRepository.getCatalogs();
        var catalogName = Functions.getCatalogName(omServiceEntity.getId());
        boolean makeCatalog = true;
        for (var catalog : catalogs) {
            if (catalog.equals(catalogName)) {
                log.info("Already created trino catalog {}", catalogName);
                makeCatalog = false;
                break;
            }
        }
        if (makeCatalog) {
            String createQuery = "create catalog " + catalogName;
            var connInfo = omServiceEntity.getConnection().getConfig();
            if (omServiceEntity.getServiceType().equalsIgnoreCase("MinIO")) {
                var optionBuilder = new StringBuilder();
                for (var optionSet : dolphinConfiguration.getHiveMetastore().getOptions().entrySet()) {
                    optionBuilder.append(" \"")
                            .append(optionSet.getKey())
                            .append("\" = '")
                            .append(optionSet.getValue())
                            .append("', ");
                }
//                createQuery = createQuery
//                        + " using hive"
//                        + " with ("
//                        + " \"hive.metastore.uri\" = '" + dolphinConfiguration.getHiveMetastore().getUri() + "', "
//                        + " \"fs.native-s3.enabled\" = 'true', "
//                        + optionBuilder
//                        + " \"s3.endpoint\" = '" + connInfo.getAwsConfig().getEndPointURL() + "', "
//                        + " \"s3.region\" = '" + connInfo.getAwsConfig().getAwsRegion() + "', "
//                        + " \"s3.aws-access-key\" = '" + connInfo.getAwsConfig().getAwsAccessKeyId() + "', "
//                        + " \"s3.aws-secret-key\" = '" + connInfo.getAwsConfig().getAwsSecretAccessKey() + "', "
//                        + " \"s3.role-session-name\" = '" + connInfo.getAwsConfig().getAssumeRoleSessionName() + "')";
                createQuery = createQuery
                        + " using storage"
                        + " with ("
                        + optionBuilder
                        + " \"hive.s3.endpoint\" = '" + connInfo.getMinioConfig().getEndPointURL() + "', "
                        + " \"hive.s3.aws-access-key\" = '" + connInfo.getMinioConfig().getAccessKeyId() + "', "
                        + " \"hive.s3.aws-secret-key\" = '" + connInfo.getMinioConfig().getSecretKey() + "')";
            } else {
                var username = connInfo.getUsername();
                String dbms;
                String password;
                dbms = omServiceEntity.getServiceType().toLowerCase();
                // 각 저장소 마다 연결 방식이 다름
                String jdbcUrl;
                switch (dbms) {
                    case "postgres" -> {
                        dbms = "postgresql";
                        jdbcUrl = "jdbc:postgresql://" + connInfo.getHostPort() + "/" + connInfo.getDatabase();
                        password = connInfo.getAuthType().getPassword();
                    }
                    case "oracle" -> {
                        // service_name 만 지원중
                        jdbcUrl = "jdbc:oracle:thin:@//" + connInfo.getHostPort() + "/" + connInfo.getOracleConnectionType().getOracleServiceName();
                        password = connInfo.getPassword();
                    }
                    case "mariadb" -> {
                        jdbcUrl = "jdbc:mariadb://" + connInfo.getHostPort();
                        password = connInfo.getPassword();
                    }
                    case "mysql" -> {
                        jdbcUrl = "jdbc:mysql://" + connInfo.getHostPort();
                        password = connInfo.getAuthType().getPassword();
                    }
                    default -> {
                        jdbcUrl = "jdbc:" + dbms + "://" + connInfo.getHostPort();
                        password = connInfo.getPassword();
                        if (password == null) {
                            password = connInfo.getAuthType().getPassword();
                        }
                    }
                }
                createQuery = createQuery
                        + " using " + dbms
                        + " with ("
                        + " \"connection-url\" = '" + jdbcUrl + "', "
                        + " \"connection-user\" = '" + username + "', "
                        + " \"connection-password\" = '" + password + "', "
                        + " \"case-insensitive-name-matching\" = 'true')";
            }
            trinoRepository.execute(createQuery);
        }
        return catalogName;
    }

    public void deleteTrinoCatalog(UUID entityId) {
        var catalogName = Functions.getCatalogName(entityId);
        var deleteQuery = "drop catalog if exists " + catalogName;
        trinoRepository.execute(deleteQuery);
    }

}
