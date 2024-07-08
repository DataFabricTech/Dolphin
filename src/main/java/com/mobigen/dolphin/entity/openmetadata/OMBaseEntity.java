package com.mobigen.dolphin.entity.openmetadata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OMBaseEntity {
    private Boolean deleted;
    private String description;
    private String displayName;
    private String fullyQualifiedName;
    private String href;
    private UUID id;
    private Boolean inherited;
    private String name;
    private EntityType type;
}
