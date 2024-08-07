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
public class OMChangeDescriptionEntity {
    private List<FieldsEntity> fieldsAdded;
    private List<FieldsEntity> fieldsUpdated;
    private List<FieldsEntity> fieldsDeleted;
    private Float previousVersion;

    @Data
    public static class FieldsEntity {
        private String name;
        private Object oldValue;
        private Object newValue;
    }
}
