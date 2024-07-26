package com.mobigen.dolphin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobigen.dolphin.dto.response.ModelDto;
import com.mobigen.dolphin.service.ModelService;
import com.mobigen.dolphin.service.QueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@WebMvcTest(controllers = ApiController.class)
class ApiControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private QueryService queryService;
    @MockBean
    private ModelService modelService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testGetModels() throws Exception {
        doReturn(List.of(
                ModelDto.builder().name("model1").build(),
                ModelDto.builder().name("model2").build()
        )).when(modelService).getModels();
        mvc.perform(get("/dolphin/v1/model"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", "model1").exists())
                .andExpect(jsonPath("$[1].name", "model2").exists());
    }

    @Test
    public void testAddModel() throws Exception {
        doReturn(ModelDto.builder().name("model1").build())
                .when(modelService).createModel(any());
        mvc.perform(post("/dolphin/v1/model")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "modelName", "model1",
                                "baseModel", Map.of("type", "CONNECTOR")
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", "model1").exists());
    }

    @Test
    public void testAddModelWithoutModelName() throws Exception {
        doReturn(ModelDto.builder().name("model1").build())
                .when(modelService).createModel(any());
        mvc.perform(post("/dolphin/v1/model")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "baseModel", Map.of("type", "CONNECTOR")
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("modelName: modelName is required value.")))
                .andExpect(jsonPath("$.message").value(is(not(containsString("baseModel: baseModel is required value.")))))
        ;
    }

    @Test
    public void testAddModelWithoutModelNameAndBaseModel() throws Exception {
        doReturn(ModelDto.builder().name("model1").build())
                .when(modelService).createModel(any());
        mvc.perform(post("/dolphin/v1/model")
                        .content(objectMapper.writeValueAsString(Map.of())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("modelName: modelName is required value.")))
                .andExpect(jsonPath("$.message").value(containsString("baseModel: baseModel is required value.")))
        ;
    }

    @Test
    public void testAddModelWithoutTypeOfBaseModel() throws Exception {
        doReturn(ModelDto.builder().name("model1").build())
                .when(modelService).createModel(any());
        mvc.perform(post("/dolphin/v1/model")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "modelName", "model1",
                                "baseModel", Map.of()
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("baseModel.type: modelType of baseModel is required value.")))
        ;
    }
}