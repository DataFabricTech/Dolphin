package com.mobigen.dolphin.controller;

import com.mobigen.dolphin.dto.request.CreateModelDto;
import com.mobigen.dolphin.dto.request.CreateModelWithFileDto;
import com.mobigen.dolphin.dto.request.ExecuteDto;
import com.mobigen.dolphin.dto.response.ModelDto;
import com.mobigen.dolphin.dto.response.QueryResultDto;
import com.mobigen.dolphin.service.ModelService;
import com.mobigen.dolphin.service.QueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Tag(name = "Dolphin Main API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/dolphin/v1")
public class ApiController {
    private final QueryService queryService;
    private final ModelService modelService;

    @Operation(summary = "Get dataModels", description = "Returns list of dataModels")
    @GetMapping("/model")
    public List<ModelDto> getModels() {
        return modelService.getModels();
    }

    @Operation(summary = "Create dataModel", description = "Create a dataModel by method (MODEL, QUERY, CONNECTOR)")
    @PostMapping("/model")
    public ModelDto addModel(@RequestBody CreateModelDto createModelDto) {
        return modelService.createModel(createModelDto);
    }

    @Operation(summary = "Create dataModel with File", description = "Create a dataModel by method (MODEL, QUERY, CONNECTOR)")
    @PostMapping(value = "/model/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ModelDto addModelWithFile(@RequestPart CreateModelWithFileDto createModelDto,
                                     @RequestPart MultipartFile file
    ) {
        return modelService.createModelWithFile(createModelDto, file);
    }

    @Operation(summary = "Execute Query using DataModel")
    @PostMapping("/query/execute")
    public QueryResultDto execute(@RequestBody ExecuteDto executeDto) {
        return queryService.execute(executeDto);
    }

    @Operation(summary = "Async Execute Query using DataModel")
    @PostMapping("/query/async/execute")
    public QueryResultDto asyncExecute(@RequestBody ExecuteDto executeDto) {
        return queryService.executeAsync(executeDto);
    }

    @Operation(summary = "Read result data of asynchronous query using JobId")
    @GetMapping("/query/read")
    public Object read(
            @RequestParam("job_id")
            @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$") String jobId,
            @RequestParam(required = false, defaultValue = "0") @Min(0) Integer offset,
            @RequestParam(required = false, defaultValue = "500") @Min(0) Integer limit
    ) {
        return queryService.read(UUID.fromString(jobId), offset, limit);
    }

    @Operation(summary = "Download result data of asynchronous query using JobId")
    @GetMapping("/query/download/{job_id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}")
    public Object download(@PathVariable("job_id") UUID jobId) {
        return null;
    }

    @Operation(summary = "Check status of asynchronous query job using JobId")
    @GetMapping("/query/status/{job_id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}")
    public Object status(@PathVariable("job_id") UUID jobId) {
        return queryService.status(jobId);
    }

    @GetMapping("/model/recommend")
    public Object recommendModels(@RequestParam(required = false, name = "fully_qualified_name") String fullyQualifiedName,
                                  @RequestParam(required = false, name = "model_id") UUID modelId) {
        return modelService.getRecommendModels(fullyQualifiedName, modelId);
    }
}
