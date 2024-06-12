package com.mobigen.dolphin.entity.openmetadata;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OMDBServiceEntity extends OMBaseEntity {
    private String serviceType;
    private ConnectionEntity connection;
    private Float version;
    private Long updatedAt;
    private String updatedBy;
    private OMBaseEntity owner;
    private OMChangeDescriptionEntity changeDescription;
}
