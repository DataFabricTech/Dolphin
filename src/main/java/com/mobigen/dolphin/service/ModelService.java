package com.mobigen.dolphin.service;

import com.mobigen.dolphin.antlr.SqlVisitor;
import com.mobigen.dolphin.config.DolphinConfiguration;
import com.mobigen.dolphin.dto.request.CreateModelDto;
import com.mobigen.dolphin.dto.request.CreateModelWithFileDto;
import com.mobigen.dolphin.dto.response.ModelDto;
import com.mobigen.dolphin.dto.response.RecommendModelDto;
import com.mobigen.dolphin.entity.local.ModelQueueEntity;
import com.mobigen.dolphin.entity.openmetadata.EntityType;
import com.mobigen.dolphin.entity.openmetadata.OMDBServiceEntity;
import com.mobigen.dolphin.entity.openmetadata.OMTableEntity;
import com.mobigen.dolphin.repository.local.FusionModelRepository;
import com.mobigen.dolphin.repository.local.ModelQueueRepository;
import com.mobigen.dolphin.repository.openmetadata.OpenMetadataRepository;
import com.mobigen.dolphin.repository.trino.TrinoRepository;
import com.mobigen.dolphin.util.Functions;
import com.mobigen.dolphin.util.IngestionType;
import com.mobigen.dolphin.util.ModelType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ModelService {
    private final TrinoRepository trinoRepository;
    private final DolphinConfiguration dolphinConfiguration;
    private final OpenMetadataRepository openMetadataRepository;
    private final FusionModelRepository fusionModelRepository;
    private final ModelQueueRepository modelQueueRepository;
    private final QueryService queryService;

    public List<ModelDto> getModels() {
        return trinoRepository.getModelList();
    }

    public String getOrCreateTrinoCatalog(OMDBServiceEntity omdbServiceEntity) {
        var catalogs = trinoRepository.getCatalogs();
        var catalogName = Functions.getCatalogName(omdbServiceEntity.getId());
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
            var connInfo = omdbServiceEntity.getConnection().getConfig();
            if (omdbServiceEntity.getServiceType().equalsIgnoreCase("S3")) {
                var optionBuilder = new StringBuilder();
                for (var optionSet : dolphinConfiguration.getHiveMetastore().getOptions().entrySet()) {
                    optionBuilder.append(" \"")
                            .append(optionSet.getKey())
                            .append("\" = '")
                            .append(optionSet.getValue())
                            .append("', ");
                }
                createQuery = createQuery
                        + " using hive"
                        + " with ("
                        + " \"hive.metastore.uri\" = '" + dolphinConfiguration.getHiveMetastore().getUri() + "', "
                        + " \"fs.native-s3.enabled\" = 'true', "
                        + optionBuilder
                        + " \"s3.endpoint\" = '" + connInfo.getAwsConfig().getEndPointURL() + "', "
                        + " \"s3.region\" = '" + connInfo.getAwsConfig().getAwsRegion() + "', "
                        + " \"s3.aws-access-key\" = '" + connInfo.getAwsConfig().getAwsAccessKeyId() + "', "
                        + " \"s3.aws-secret-key\" = '" + connInfo.getAwsConfig().getAwsSecretAccessKey() + "', "
                        + " \"s3.role-session-name\" = '" + connInfo.getAwsConfig().getAssumeRoleSessionName() + "')";
            } else {
                var username = connInfo.getUsername();
                String dbms;
                String password;
                if ("postgres".equalsIgnoreCase(omdbServiceEntity.getServiceType())) {
                    dbms = "postgresql";
                    password = connInfo.getAuthType().getPassword();
                } else {
                    dbms = omdbServiceEntity.getServiceType().toLowerCase();
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

    public ModelDto createModel(CreateModelDto createModelDto) {
        var selectedColumns = !createModelDto.getBaseModel().getSelectedColumnNames().isEmpty() ?
                createModelDto.getBaseModel().getSelectedColumnNames().stream()
                        .map(Functions::convertKeywordName)
                        .collect(Collectors.joining(", "))
                : "*";
        var trinoModel = dolphinConfiguration.getModel().getCatalog()
                + "." + dolphinConfiguration.getModel().getSchema().getDb()
                + "." + createModelDto.getModelName();
        String sql = "create view " + trinoModel;

        if (createModelDto.getBaseModel().getType() == ModelType.CONNECTOR) {
            OMDBServiceEntity connInfo;
            if (createModelDto.getBaseModel().getConnectorId() != null) {
                connInfo = openMetadataRepository.getConnectorInfo(createModelDto.getBaseModel().getConnectorId(),
                        EntityType.DATABASE_SERVICE);
            } else {
                connInfo = openMetadataRepository.getConnectorInfo(createModelDto.getBaseModel().getConnectorFQN(),
                        EntityType.DATABASE_SERVICE);
            }
            System.out.println(connInfo);
            var catalogName = getOrCreateTrinoCatalog(connInfo);
            sql = sql + " as select " + selectedColumns
                    + " from " + catalogName
                    + "." + createModelDto.getBaseModel().getDatabase()
                    + "." + createModelDto.getBaseModel().getTable();
            String fqn;
            if ("postgresql".contains(connInfo.getServiceType().toLowerCase())) {
                fqn = connInfo.getFullyQualifiedName()
                        + "." + createModelDto.getBaseModel().getDatabase()
                        + ".public"
                        + "." + createModelDto.getBaseModel().getTable();
            } else {  // mariadb, mysql
                fqn = connInfo.getFullyQualifiedName()
                        + "." + createModelDto.getBaseModel().getSchema()
                        + "." + createModelDto.getBaseModel().getDatabase()
                        + "." + createModelDto.getBaseModel().getTable();
            }
            var modelQueueEntity = ModelQueueEntity.builder()
                    .trinoModelName(trinoModel)
                    .modelNameFqn(dolphinConfiguration.getModel().getOmTrinoDatabaseService() + "." + trinoModel)
                    .fromFqn(fqn)
                    .command(ModelQueueEntity.Command.LINEAGE)
                    .build();
            trinoRepository.execute(sql);
            modelQueueRepository.save(modelQueueEntity);
        } else if (createModelDto.getBaseModel().getType() == ModelType.MODEL) {
            sql = sql + " as select " + selectedColumns
                    + " from " + dolphinConfiguration.getModel().getCatalog()
                    + "." + dolphinConfiguration.getModel().getSchema().getDb()
                    + "." + createModelDto.getBaseModel().getModel();
            trinoRepository.execute(sql);
        } else {
            var visitor = new SqlVisitor(null,
                    openMetadataRepository,
                    dolphinConfiguration,
                    createModelDto.getBaseModel().getReferenceModels());
            var parseTree = queryService.getParseTree(createModelDto.getBaseModel().getQuery());
            var convertedQuery = queryService.getConvertedSql(visitor, parseTree);
            sql = sql + " as " + convertedQuery;
            List<ModelQueueEntity> modelQueueEntities = new ArrayList<>();
            for (var modelHistory : visitor.getUsedModelHistory()) {
                modelQueueEntities.add(ModelQueueEntity.builder()
                        .trinoModelName(trinoModel)
                        .modelNameFqn(dolphinConfiguration.getModel().getOmTrinoDatabaseService() + "." + trinoModel)
                        .fromFqn(modelHistory.getFullyQualifiedName())
                        .command(ModelQueueEntity.Command.LINEAGE)
                        .build());
            }
            trinoRepository.execute(sql);
            modelQueueRepository.saveAll(modelQueueEntities);
        }
        openMetadataRepository.callIngestion(IngestionType.METADATA);
        return ModelDto.builder()
                .name(createModelDto.getModelName())
                .build();
    }

    public ModelDto createModelWithFile(CreateModelWithFileDto createModelDto, MultipartFile file) {
        // TODO : upload file to s3(minio) - FMS 로 대체 가능?
        // TODO : create model using trino-hive-s3(minio)
        var connInfo = openMetadataRepository.getConnectorInfo(createModelDto.getStorageId(), EntityType.STORAGE_SERVICE);
        var catalog = getOrCreateTrinoCatalog(connInfo);
        return ModelDto.builder()
                .name(createModelDto.getModelName())
                .build();
    }

    public List<RecommendModelDto> getRecommendModels(String fullyQualifiedName, UUID modelId) {
        // select model_id, fqn from fusionModel where job_id in (select job_id from fusionModel where fqn = '' or model_id = '')
        return fusionModelRepository.findAllByFullyQualifiedNameOrModelIdOfOM(fullyQualifiedName, modelId);
    }

    public List<OMDBServiceEntity> getOpenMetadataDBServices(String fields, String domain, Integer limit) {
        return openMetadataRepository.getConnectors(fields, domain, limit);
    }

    public List<OMTableEntity> getOpenMetadataTables(String fields, String database, String databaseSchema, Integer limit) {
        return openMetadataRepository.getTables(fields, database, databaseSchema, limit);
    }
}
