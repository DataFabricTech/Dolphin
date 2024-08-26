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
                if ("postgres".equalsIgnoreCase(omServiceEntity.getServiceType())) {
                    dbms = "postgresql";
                    password = connInfo.getAuthType().getPassword();
                } else {
                    dbms = omServiceEntity.getServiceType().toLowerCase();
                    password = connInfo.getPassword();
                    if (password == null) {
                        password = connInfo.getAuthType().getPassword();
                    }
                }
                var jdbcURL = "jdbc:" + dbms + "://" + connInfo.getHostPort();
                if (!List.of("mariadb", "mysql").contains(dbms)  // mariadb/mysql 의 경우 trino 에서 jdbc-url 에 db 세팅을 하지 않도록 되어 있어서 제외
                        && connInfo.getDatabase() != null && !connInfo.getDatabase().isEmpty()) {
                    jdbcURL = jdbcURL + "/" + connInfo.getDatabase();
                }

                createQuery = createQuery
                        + " using " + dbms
                        + " with ("
                        + " \"connection-url\" = '" + jdbcURL + "', "
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
