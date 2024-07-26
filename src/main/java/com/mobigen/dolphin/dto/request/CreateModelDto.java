package com.mobigen.dolphin.dto.request;

import com.mobigen.dolphin.util.JoinType;
import com.mobigen.dolphin.util.ModelType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
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
public class CreateModelDto {
    @Schema(description = "DataModel name to create", example = "model_test_1")
    private String modelName;

    @Schema(description = "Conditions of base DataModel")
    private BaseModel baseModel;
    @Schema(description = "Conditions for join")
    private List<JoinModel> joins;

    @Getter
    @Setter
    public static class BaseModel {
        @Schema(description = "Type of base DataModel (MODEL, QUERY, CONNECTOR)")
        private ModelType type;
        // QUERY
        @Schema(description = "Sql select query using DataModel", example = "select * from model_test_1")
        private String query;
        @Schema(description = "Model information of OpenMetadata referenced by the model used in the sql query")
        private List<ExecuteDto.ReferenceModel> referenceModels = new ArrayList<>();

        // Shared by MODEL, CONNECTOR
        @Schema(description = "Select columns, default = *")
        private List<String> selectedColumnNames = new ArrayList<>();
        // MODEL
        @Schema(description = "DataModel name")
        private String model;
        // CONNECTOR
        @Schema(description = "Id of OpenMetadata DBService.\n*connectorId* is a high priority.")
        private UUID connectorId;
        @Schema(description = "Fully Qualified Name of OpenMetadata DBService.\nIf *connectorId* is not set, use FQN.")
        private String connectorFQN;
        @Schema(description = "Database name")
        private String database;
        @Schema(description = "Schema name")
        private String schema;
        @Schema(description = "Table name")
        private String table;
    }

    @Getter
    @Setter
    public static class JoinModel {
        // TODO create model: join 모델/컨넥터/쿼리 등 할 수 있게 추가 필요
        private JoinType joinType;
        private String model;
        private String on;
    }
}
