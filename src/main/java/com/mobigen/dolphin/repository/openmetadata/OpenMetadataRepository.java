package com.mobigen.dolphin.repository.openmetadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobigen.dolphin.config.DolphinConfiguration;
import com.mobigen.dolphin.entity.openmetadata.*;
import com.mobigen.dolphin.util.IngestionType;
import jakarta.validation.constraints.AssertTrue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

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

    // URI/URL = https://www.mobigen.com/test?size=1
    // URI = www.mobigen.com/test?size=1
    // URN = test?size=1

    private URI getUri(String urn) {
        return URI.create(Path.of(dolphinConfiguration.getOpenMetadata().getApiUrl(), urn).toString());
    }

    private WebClient getWebClient() {
        return WebClient.builder()
                .baseUrl(dolphinConfiguration.getOpenMetadata().getApiUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, dolphinConfiguration.getOpenMetadata().getBotToken())
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

    public List<OMDBServiceEntity> getConnectors(String fields, String domain, Integer limit) {
        var response = getResponse(uriBuilder -> {
            uriBuilder = uriBuilder
                    .path("/v1/services/databaseServices");
            if (fields != null) {
                uriBuilder = uriBuilder.queryParam("fields", fields);
            }
            if (domain != null) {
                uriBuilder = uriBuilder.queryParam("domain", domain);
            }
            if (limit != null) {
                uriBuilder = uriBuilder.queryParam("limit", limit);
            }
            return uriBuilder.build();
        });

        var mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            var responseJson = mapper.readTree(response);
            var connectors = new ArrayList<OMDBServiceEntity>();
            for (JsonNode jsonNode : responseJson.get("data")) {
                connectors.add(mapper.convertValue(jsonNode, OMDBServiceEntity.class));
            }
            return connectors;
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return List.of();
        }
    }

    public List<OMTableEntity> getTables(String fields, String database, String databaseSchema, Integer limit) {
        var response = getResponse(uriBuilder -> {
            uriBuilder = uriBuilder
                    .path("/v1/tables");
            if (fields != null) {
                uriBuilder = uriBuilder.queryParam("fields", fields);
            }
            if (database != null) {
                uriBuilder = uriBuilder.queryParam("database", database);
            }
            if (databaseSchema != null) {
                uriBuilder = uriBuilder.queryParam("databaseSchema", databaseSchema);
            }
            if (limit != null) {
                uriBuilder = uriBuilder.queryParam("limit", limit);
            }
            return uriBuilder.build();
        });
        var mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            var responseJson = mapper.readTree(response);
            var tables = new ArrayList<OMTableEntity>();
            for (JsonNode jsonNode : responseJson.get("data")) {
                tables.add(mapper.convertValue(jsonNode, OMTableEntity.class));
            }
            return tables;
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return List.of();
        }
    }

    @AssertTrue(message = "Fail to get databaseService information from OpenMetadata")
    public OMDBServiceEntity getConnectorInfo(UUID id, EntityType entityType) {
        String urn;
        if (entityType.equals(EntityType.DATABASE_SERVICE)) {
            urn = "/v1/services/databaseServices/" + id;
        } else {
            urn = "/v1/services/storageServices/" + id;
        }
        var webClient = getWebClient();
        var response = webClient.get()
                .uri(urn)
                .retrieve()
                .bodyToMono(OMDBServiceEntity.class)
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
        log.info(Objects.requireNonNull(response).toString());
        return response;
    }

    @AssertTrue(message = "Fail to get table information from OpenMetadata")
    public OMTableEntity getTable(String fullyQualifiedName) {
        return (OMTableEntity) getTable(fullyQualifiedName, OMTableEntity.class);
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
        log.info(Objects.requireNonNull(response).toString());
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
}
