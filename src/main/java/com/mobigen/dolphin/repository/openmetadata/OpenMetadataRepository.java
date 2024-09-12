package com.mobigen.dolphin.repository.openmetadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobigen.dolphin.config.DolphinConfiguration;
import com.mobigen.dolphin.dto.request.CreateModelDto;
import com.mobigen.dolphin.dto.response.QueryResultDto;
import com.mobigen.dolphin.entity.openmetadata.*;
import com.mobigen.dolphin.util.IngestionType;
import com.mobigen.dolphin.util.ModelType;
import jakarta.validation.constraints.AssertTrue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @AssertTrue(message = "Fail to get table information from OpenMetadata")
    public OMTableEntity getTableOrContainer(String fullyQualifiedName) {
        try {
            return (OMTableEntity) getTable(fullyQualifiedName, OMTableEntity.class);
        } catch (Exception e) {
            try {
                log.warn(fullyQualifiedName + " is not a table");
                return (OMTableEntity) getContainer(fullyQualifiedName, OMTableEntity.class);
            } catch (Exception e1) {
                log.error(e1.getMessage(), e1);
                throw e1;
            }
        }
        // eyJraWQiOiJHYjM4OWEtOWY3Ni1nZGpzLWE5MmotMDI0MmJrOTQzNTYiLCJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJvcGVuLW1ldGFkYXRhLm9yZyIsInN1YiI6ImRhdGEtZW5naW5lLWJvdCIsInJvbGVzIjpbIkluZ2VzdGlvbkJvdFJvbGUiLCJQcm9maWxlckJvdFJvbGUiXSwiZW1haWwiOiJkYXRhZW5naW5lQG1vYmlnZW4uY29tIiwiaXNCb3QiOnRydWUsInRva2VuVHlwZSI6IkJPVCIsImlhdCI6MTcyNjAzNTMwMSwiZXhwIjpudWxsfQ.jCnL0D4ZvbBBJxCMoLeArJNtQxAhsE2q6bk_BEFrMs1-oPa_HUOA8X2DVVLgbMg04mDWNTKnH9BPIH57sImDU_eMerRYiEBY68LV8SL6IKqxIqXdZ6BF3Nzd9K2HqDIuSKnyWlAyF7JIhIu5noldH7m1GnDMgGu8PXPeCL12CDgbFVsIiTGuNPJSTK0h8DESsFTkpLoca5JwGKBPneS_ZUuSRkx0CshLTRcg4qpGt3joqWXctRQp9MpFxpVrCo9SWE0QDAqb2B7u7AhFoH2iKa-r_D_jphXrW92KdnFsAc26ZPT5_orUanm24YL3zbrVZWFsrz4u8FIGLYy_Kfj5Fw
//      "id": "cbc91a69-e02f-492f-bc1c-06e93f1517d8",
//      "botUser.id": "d016caf4-21f3-4e85-a474-efbaff5ce4bb",
//      "eyJraWQiOiJHYjM4OWEtOWY3Ni1nZGpzLWE5MmotMDI0MmJrOTQzNTYiLCJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJvcGVuLW1ldGFkYXRhLm9yZyIsInN1YiI6ImRhdGEtZW5naW5lLWJvdCIsInJvbGVzIjpbIkFwcGxpY2F0aW9uQm90Um9sZSIsIkluZ2VzdGlvbkJvdFJvbGUiLCJQcm9maWxlckJvdFJvbGUiLCJEYXRhQ29uc3VtZXIiXSwiZW1haWwiOiJkYXRhZW5naW5lQG1vYmlnZW4uY29tIiwiaXNCb3QiOnRydWUsInRva2VuVHlwZSI6IkJPVCIsImlhdCI6MTcyNjAzNjkzMCwiZXhwIjoxNzI2MDQwNTMwfQ.XA8uo22bEjtAKoz23DovZN4y6DKCkcHaM2n1eUE3im7Us0K5V4bQTko69-1qdCUV5Tq-y97fypZgC3TLr118VqC_zYMiVteZo-sdiMx78_yAISZwvc13IHzZz_wyZ5FEeInklsmGBHd22L0Y7LIn_8hcQFKWxx-iUeXq_Ap65ur84gKMkiqUMgpj-IwKLvA3QnClOYM__0bKA1mcWNfBj8aZiflqIWO9wXNa-mEgd_D2_KQzdzrIB0BuBFDFB6cvONnJoP1bbczEUi560qp3EIMVss_Dq_C6Aen2PKjxRAUEf27rSURQJRtaGwUbEMJOK7BPaBCZLmGfk4WULYIi1g",
    }

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
        // Create Table
        var response = getWebClient().put()
                .uri("/v1/tables")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(table))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        log.info(response);

        // Add Lineage
        for (Map.Entry<String, String> entry : lineage.entrySet()) {
            var from = getTable(entry.getKey(), OMTableEntity.class);
            var to = getTable(entry.getValue(), OMTableEntity.class);
            if (from != null && to != null) {
                AddLineage addLineage = new AddLineage().withEdge(
                        new EntitiesEdge()
                                .withFromEntity(new EntityReference().withId(from.getId()).withType("table"))
                                .withToEntity(new EntityReference().withId(to.getId()).withType("table"))
                );
                response = getWebClient().put()
                        .uri("/v1/lineage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(addLineage))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            }
        }
    }
}
