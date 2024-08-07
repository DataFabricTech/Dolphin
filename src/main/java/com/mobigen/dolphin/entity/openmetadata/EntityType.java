package com.mobigen.dolphin.entity.openmetadata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public enum EntityType {
    DATABASE_SERVICE("databaseService"),
    STORAGE_SERVICE("storageService"),
    DATABASE_SCHEMA("databaseSchema"),
    DATABASE("database"),
    @JsonProperty("table")
    TABLE("table"),
    INGESTION_PIPELINE("ingestionPipeline"),
    UNSUPPORTED("unsupported"),
    ;

    private final String value;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static EntityType from(String value) {
        for (EntityType type : EntityType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        log.warn("Unsupported entity type: {}", value);
        return UNSUPPORTED;
    }
}