package com.mobigen.dolphin.repository.openmetadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobigen.dolphin.aop.CheckOpenMetadata;
import com.mobigen.dolphin.aop.WriteAroundLogging;
import com.mobigen.dolphin.config.DolphinConfiguration;
import com.mobigen.dolphin.dto.request.CreateModelDto;
import com.mobigen.dolphin.dto.response.QueryResultDto;
import com.mobigen.dolphin.entity.openmetadata.*;
import com.mobigen.dolphin.util.IngestionType;
import com.mobigen.dolphin.util.ModelType;
import com.mobigen.dolphin.util.Pair;
import jakarta.validation.constraints.AssertTrue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openmetadata.schema.api.data.CreateQuery;
import org.openmetadata.schema.api.data.CreateTable;
import org.openmetadata.schema.api.lineage.AddLineage;
import org.openmetadata.schema.type.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
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
@Repository
public class OpenMetadataRepository {
    private final DolphinConfiguration dolphinConfiguration;
    private static final String DATA_ENGINE_BOT_NAME = "ingestion-bot";
    private String token;
    private String botID;

    private URI getUri(String urn) {
        return URI.create(Path.of(dolphinConfiguration.getOpenMetadata().getApiUrl(), urn).toString());
    }

    public void initBot() {
        // Get DataEngine(Ingestion) Bot Token
        getDataEngineToken();
        log.info("SUCCESS. Get DataEngine(Ingestion) Bot Token");
    }

    private void getDataEngineToken() {
        try {
            WebClient client = WebClient.builder()
                    .baseUrl(dolphinConfiguration.getOpenMetadata().getApiUrl())
                    .defaultHeader(HttpHeaders.AUTHORIZATION, dolphinConfiguration.getOpenMetadata().getBotToken())
                    .build();
            var res = client.get()
                    .uri("/v1/users/name/" + DATA_ENGINE_BOT_NAME)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            var mapper = new ObjectMapper();
            JsonNode botJsonRoot = mapper.readTree(res);
            this.botID = botJsonRoot.get("id").asText();
            getToken(botID);
        } catch (JsonProcessingException | WebClientResponseException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Fail To Get Data Engine Bot Token");
        }
    }

    private void getToken(String botID) {
        try {
            WebClient client = WebClient.builder()
                    .baseUrl(dolphinConfiguration.getOpenMetadata().getApiUrl())
                    .defaultHeader(HttpHeaders.AUTHORIZATION, dolphinConfiguration.getOpenMetadata().getBotToken())
                    .build();
            var res = client.get()
                    .uri("/v1/users/token/" + botID)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            var mapper = new ObjectMapper();
            JsonNode tokenJsonRoot = mapper.readTree(res);
            this.token = tokenJsonRoot.get("JWTToken").asText();
        } catch (JsonProcessingException | WebClientResponseException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Fail To Get Data Engine Bot Token");
        }
    }

//    private void createDataEngine() {
//        createDataEngineUser();
//        createDataEngineBot();
//    }
//
//    private void createDataEngineUser() {
//        List<UUID> roles = getRoles();
//        if (roles == null) {
//            log.error("Fail To Get Role Information From Metadata Server");
//            throw new RuntimeException("Fail To Get Role Information From Metadata Server");
//        }
//        CreateUser bot = new CreateUser()
//                .withEmail("dataengine@mobigen.com")
//                .withName(DATA_ENGINE_BOT_NAME)
//                .withBotName(DATA_ENGINE_BOT_NAME)
//                .withDisplayName("Data Engine Bot")
//                .withDescription("데이터 융합/정제를 위한 데이터 엔진 봇")
//                .withIsBot(true)
//                .withRoles(roles)
//                .withAuthenticationMechanism(new AuthenticationMechanism()
//                        .withAuthType(AuthenticationMechanism.AuthType.JWT)
//                        .withConfig(Map.of("JWTTokenExpiry", "Unlimited")));
//        try {
//            WebClient client = WebClient.builder()
//                    .baseUrl(dolphinConfiguration.getOpenMetadata().getApiUrl())
//                    .defaultHeader(HttpHeaders.AUTHORIZATION, dolphinConfiguration.getOpenMetadata().getBotToken())
//                    .build();
//            var res = client.put()
//                    .uri("/v1/users")
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .accept(MediaType.APPLICATION_JSON)
//                    .body(BodyInserters.fromValue(bot))
//                    .retrieve()
//                    .bodyToMono(String.class)
//                    .block();
//            var mapper = new ObjectMapper();
//            JsonNode botJsonRoot = mapper.readTree(res);
//            this.token = botJsonRoot.get("authenticationMechanism").get("config").get("JWTToken").asText();
//        } catch (JsonProcessingException | WebClientResponseException e) {
//            log.error(e.getMessage(), e);
//            throw new RuntimeException("Fail To Create Data Engine User");
//        }
//    }
//
//    private void createDataEngineBot() {
//        CreateBot bot = new CreateBot()
//                .withBotUser(DATA_ENGINE_BOT_NAME)
//                .withName(DATA_ENGINE_BOT_NAME)
//                .withDisplayName("Data Engine Bot")
//                .withDescription("데이터 융합/정제를 위한 데이터 엔진 봇")
//                .withProvider(ProviderType.SYSTEM);
//
//        try {
//            WebClient client = WebClient.builder()
//                    .baseUrl(dolphinConfiguration.getOpenMetadata().getApiUrl())
//                    .defaultHeader(HttpHeaders.AUTHORIZATION, dolphinConfiguration.getOpenMetadata().getBotToken())
//                    .build();
//            client.post()
//                    .uri("/v1/bots")
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .accept(MediaType.APPLICATION_JSON)
//                    .body(BodyInserters.fromValue(bot))
//                    .retrieve()
//                    .bodyToMono(String.class)
//                    .block();
//        } catch ( WebClientResponseException e ) {
//            log.error(e.getMessage(), e);
//            throw new RuntimeException("Fail To Create Data Engine Bot");
//        }
//    }
//
//    private List<UUID> getRoles() {
//        // Get Role ID - Ingestion bot role, Profiler bot role
//        try {
//            WebClient client = WebClient.builder()
//                    .baseUrl(dolphinConfiguration.getOpenMetadata().getApiUrl())
//                    .defaultHeader(HttpHeaders.AUTHORIZATION, dolphinConfiguration.getOpenMetadata().getBotToken())
//                    .build();
//            var res = client.get()
//                    .uri("/v1/roles")
//                    .accept(MediaType.APPLICATION_JSON)
//                    .retrieve()
//                    .bodyToMono(String.class)
//                    .block();
//            var mapper = new ObjectMapper();
//            JsonNode roleJsonRoot = mapper.readTree(res);
//            var roles = new ArrayList<UUID>();
//            for (JsonNode role : roleJsonRoot.get("data")) {
//                if (role.get("name").asText().equals("IngestionBotRole") || role.get("name").asText().equals("ProfilerBotRole")) {
//                    roles.add(UUID.fromString(role.get("id").asText()));
//                }
//            }
//            return roles;
//        } catch (JsonProcessingException | WebClientResponseException e) {
//            log.error(e.getMessage(), e);
//        }
//        return null;
//    }

    private WebClient getWebClient() {
        return WebClient.builder()
                .baseUrl(dolphinConfiguration.getOpenMetadata().getApiUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + this.token)
                .build();
    }

    public String getResponse(Function<UriBuilder, URI> uriFunction) {
        var webClient = getWebClient();
        return webClient.get()
                .uri(uriFunction)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String getResponse(String path, Map<String, String> options) {
        return getResponse(uriBuilder -> {
            uriBuilder = uriBuilder.path(path);
            for (Map.Entry<String, String> entry : options.entrySet()) {
                uriBuilder = uriBuilder.queryParam(entry.getKey(), entry.getValue());
            }
            return uriBuilder.build();
        });
    }

    public List<OMServiceEntity> getConnectors(String fields, String domain, Integer limit) {
        Map<String, String> options = new HashMap<>();
        if (fields != null) {
            options.put("fields", fields);
        }
        if (domain != null) {
            options.put("domain", fields);
        }
        if (limit != null) {
            options.put("limit", fields);
        }
        var responseDB = getResponse("/v1/services/databaseServices", options);
        var responseStorage = getResponse("/v1/services/storageServices", options);
        var mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            var responseDBJson = mapper.readTree(responseDB);
            var connectors = new ArrayList<OMServiceEntity>();
            for (JsonNode jsonNode : responseDBJson.get("data")) {
                connectors.add(mapper.convertValue(jsonNode, OMServiceEntity.class));
            }
            var responseStorageJson = mapper.readTree(responseStorage);
            for (JsonNode jsonNode : responseStorageJson.get("data")) {
                connectors.add(mapper.convertValue(jsonNode, OMServiceEntity.class));
            }
            return connectors;
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return List.of();
        }
    }

    public List<OMTableEntity> getTables(String fields, String database, String databaseSchema, Integer limit) {
        Map<String, String> options = new HashMap<>();
        if (fields != null) {
            options.put("fields", fields);
        }
        if (database != null) {
            options.put("database", fields);
        }
        if (databaseSchema != null) {
            options.put("databaseSchema", fields);
        }
        if (limit != null) {
            options.put("limit", fields);
        }
        var responseTable = getResponse("/v1/tables", options);
        options.put("fields", "children");
        var responseContainer = getResponse("/v1/containers", options);
        var mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            var responseTableJson = mapper.readTree(responseTable);
            var tables = new ArrayList<OMTableEntity>();
            for (JsonNode jsonNode : responseTableJson.get("data")) {
                tables.add(mapper.convertValue(jsonNode, OMTableEntity.class));
            }
            var responseContainerJson = mapper.readTree(responseContainer);
            for (JsonNode jsonNode : responseContainerJson.get("data")) {
                tables.add(mapper.convertValue(jsonNode, OMTableEntity.class));
            }
            return tables;
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return List.of();
        }
    }

    @CheckOpenMetadata
    @WriteAroundLogging
    @AssertTrue(message = "Fail to get databaseService information from OpenMetadata")
    public OMServiceEntity getConnectorInfo(UUID id, EntityType entityType) {
        String urn;
        if (entityType.equals(EntityType.DATABASE_SERVICE)) {
            urn = "/v1/services/databaseServices/" + id + "?fields=*";
        } else {
            urn = "/v1/services/storageServices/" + id + "?fields=*";
        }
        var webClient = getWebClient();
        var response = webClient.get()
                .uri(urn)
                .retrieve()
                .bodyToMono(OMServiceEntity.class)
                .block();
        log.info(Objects.requireNonNull(response).toString());
        return response;
    }

    @CheckOpenMetadata
    @WriteAroundLogging
    @AssertTrue(message = "Fail to get databaseService information from OpenMetadata")
    public OMServiceEntity getConnectorInfo(String fqn, EntityType entityType) {
        String urn;
        if (entityType.equals(EntityType.DATABASE_SERVICE)) {
            urn = "/v1/services/databaseServices/name/" + fqn + "?fields=*";
        } else {
            urn = "/v1/services/storageServices/name/" + fqn + "?fields=*";
        }
        var webClient = getWebClient();
        var response = webClient.get()
                .uri(urn)
                .retrieve()
                .bodyToMono(OMServiceEntity.class)
                .block();
        log.info(Objects.requireNonNull(response).toString());
        return response;
    }

    @CheckOpenMetadata
    @WriteAroundLogging
    @AssertTrue(message = "Fail to get table information from OpenMetadata")
    public OMTableEntity getTable(UUID id) {
        var webClient = getWebClient();
        var response = webClient.get()
                .uri("/v1/tables/" + id)
                .retrieve()
                .bodyToMono(OMTableEntity.class)
                .block();
        log.info("tableName: {}, fqn: {}",
                Objects.requireNonNull(response).getName(),
                Objects.requireNonNull(response).getFullyQualifiedName());
        return response;
    }

    @CheckOpenMetadata
    @WriteAroundLogging
    @AssertTrue(message = "Fail to get table information from OpenMetadata")
    public OMTableEntity getTableOrContainer(String fullyQualifiedName) {
        return getTableOrContainerWithType(fullyQualifiedName).left();
    }

    public Pair<OMTableEntity, String> getTableOrContainerWithType(String fullyQualifiedName) {
        try {
            return new Pair<>((OMTableEntity) getTable(fullyQualifiedName, OMTableEntity.class), "table");
        } catch (Exception e) {
            try {
                log.warn(fullyQualifiedName + " is not a table");
                return new Pair<>((OMTableEntity) getContainer(fullyQualifiedName, OMTableEntity.class), "container");
            } catch (Exception e1) {
                log.error(e1.getMessage(), e1);
                throw e1;
            }
        }
    }

    @CheckOpenMetadata
    @WriteAroundLogging
    @AssertTrue(message = "Fail to get table information from OpenMetadata")
    public OMBaseEntity getTable(String fullyQualifiedName, Class<? extends OMBaseEntity> outputClazz) {
        var webClient = getWebClient();
        var response = webClient.get()
                .uri("/v1/tables/name/" + fullyQualifiedName)
                .retrieve()
                .bodyToMono(outputClazz)
                .retry(0)
                .block();
        log.info("tableName: {}, fqn: {}",
                Objects.requireNonNull(response).getName(),
                Objects.requireNonNull(response).getFullyQualifiedName());
        return response;
    }

    @CheckOpenMetadata
    @WriteAroundLogging
    @AssertTrue(message = "Fail to get container information from OpenMetadata")
    public OMBaseEntity getContainer(String fullyQualifiedName, Class<? extends OMBaseEntity> outputClazz) {
        var webClient = getWebClient();
        var response = webClient.get()
                .uri("/v1/containers/name/" + fullyQualifiedName)
                .retrieve()
                .bodyToMono(outputClazz)
                .retry(0)
                .block();
        log.info("containerName: {}, fqn: {}",
                Objects.requireNonNull(response).getName(),
                Objects.requireNonNull(response).getFullyQualifiedName());
        return response;
    }

    @CheckOpenMetadata
    @WriteAroundLogging
    @AssertTrue(message = "Fail to get user information from OpenMetadata")
    public EntityReference getUser(String name) {
        var webClient = getWebClient();
        var response = webClient.get()
                .uri("/v1/users/name/" + name)
                .retrieve()
                .bodyToMono(EntityReference.class)
                .retry(0)
                .block();
        assert response != null;
        response.setType("user");
        log.info("{} name: {}, fqn: {}",
                Objects.requireNonNull(response).getType(),
                Objects.requireNonNull(response).getName(),
                Objects.requireNonNull(response).getFullyQualifiedName());
        return response;
    }


    @CheckOpenMetadata
    @WriteAroundLogging
    public void addLineageEdge(LineageEdgeEntity lineageEdgeEntity) throws JsonProcessingException {
        var webClient = getWebClient();
        var om = new ObjectMapper();
        var data = om.writeValueAsString(Map.of("edge", lineageEdgeEntity));
        var response = webClient.put()
                .uri("/v1/lineage")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(data))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        log.info(response);
    }

    public void callIngestion(IngestionType type) {
        // metadata ingestion 만 호출
        switch (type) {
            case METADATA -> callIngestion(dolphinConfiguration.getOpenMetadata().getIngestion().getMetadata());
            case PROFILER -> callIngestion(dolphinConfiguration.getOpenMetadata().getIngestion().getProfiler());
        }
    }

    @CheckOpenMetadata
    @WriteAroundLogging
    public void callIngestion(String ingestionId) {
        log.info("Call ingestion id: {}", ingestionId);
        var webClient = getWebClient();
        var response = webClient.post()
                .uri("/v1/services/ingestionPipelines/trigger/" + ingestionId)
                .header(HttpHeaders.HOST, dolphinConfiguration.getOpenMetadata().getApiUrl())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        log.info("Result of ingestion, id: {}, result: {}", ingestionId, response);
    }

    public void createModel(CreateModelDto createModelDto, Map<String, String> lineage, QueryResultDto resultDto) {
        List<Column> columns = new ArrayList<>();
        for (QueryResultDto.Column column : resultDto.getColumns()) {
            switch (column.getDataType()) {
                case BOOL -> columns.add(new Column()
                        .withName(column.getName())
                        .withDataType(ColumnDataType.BOOLEAN));
                case TEXT -> columns.add(new Column()
                        .withName(column.getName())
                        .withDataType(ColumnDataType.TEXT));
                case INTEGER -> columns.add(new Column()
                        .withName(column.getName())
                        .withDataType(ColumnDataType.INT));
                case BIGINT -> columns.add(new Column()
                        .withName(column.getName())
                        .withDataType(ColumnDataType.BIGINT));
                case REAL -> columns.add(new Column()
                        .withName(column.getName())
                        .withDataType(ColumnDataType.DOUBLE));
                case UNDEFINED -> columns.add(new Column()
                        .withName(column.getName())
                        .withDataType(ColumnDataType.UNKNOWN));
            }
        }
        CreateTable table = new CreateTable()
                .withName(createModelDto.getModelName())
                .withDisplayName(createModelDto.getDisplayName())
                .withDescription(createModelDto.getDescription())
                .withTags(createModelDto.getTags().stream().map(e ->
                        new TagLabel().withTagFQN(e)).collect(Collectors.toList()))
                .withDatabaseSchema(String.format("%s.%s.%s",
                        dolphinConfiguration.getModel().getOmTrinoDatabaseService(),
                        dolphinConfiguration.getModel().getCatalog(),
                        dolphinConfiguration.getModel().getSchema().getDb()))
                .withColumns(columns)
                .withTableType(TableType.View);
        if (createModelDto.getBaseModel().getType() == ModelType.QUERY) {
            table.withSchemaDefinition(createModelDto.getBaseModel().getQuery());
        }
        if (createModelDto.getOwner() != null) {
            table.withOwner(getUser(createModelDto.getOwner()));
        }
        // Create Table
        var response = getWebClient().put()
                .uri("/v1/tables")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(table))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        log.info(response);

        if (!lineage.isEmpty()) {
            try {
                // Add Lineage
                List<Pair<OMTableEntity, String>> usedModels = new ArrayList<>();
                var to = getTableOrContainerWithType(lineage.get(lineage.keySet().iterator().next()));
                for (Map.Entry<String, String> entry : lineage.entrySet()) {
                    var from = getTableOrContainerWithType(entry.getKey());
                    usedModels.add(from);
                    AddLineage addLineage = new AddLineage().withEdge(
                            new EntitiesEdge()
                                    .withFromEntity(new EntityReference().withId(from.left().getId()).withType(from.right()))
                                    .withToEntity(new EntityReference().withId(to.left().getId()).withType(to.right()))
                    );
                    response = getWebClient().put()
                            .uri("/v1/lineage")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.fromValue(addLineage))
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();
                    log.info(response);
                }
                // Add Query
                if (createModelDto.getBaseModel().getType().equals(ModelType.QUERY)) {
                    response = getWebClient().put()
                            .uri("/v1/queries")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.fromValue(new CreateQuery()
                                    .withQuery("/* Created model (" + createModelDto.getModelName() + ") by */\n"
                                            + createModelDto.getBaseModel().getQuery())
                                    .withQueryDate(Instant.now().toEpochMilli())
                                    .withOwner(table.getOwner())
                                    .withService(to.left().getService().getName())
                                    .withQueryUsedIn(usedModels.stream().map(model -> new EntityReference()
                                            .withId(model.left().getId())
                                            .withType(model.right())).toList())
                            ))
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();
                    log.info(response);
                }
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        }
    }
}
