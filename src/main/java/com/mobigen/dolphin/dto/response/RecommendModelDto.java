package com.mobigen.dolphin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Data
@AllArgsConstructor
public class RecommendModelDto {
    private UUID modelId;
    private String fqn;
    private Long count;
}
