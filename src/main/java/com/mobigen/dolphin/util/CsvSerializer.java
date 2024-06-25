package com.mobigen.dolphin.util;

import com.mobigen.dolphin.dto.response.QueryResultDTO;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class CsvSerializer {
    public static QueryResultDTO readCsv(String path) {
        try (var fileReader = new FileReader(path);
             var csvReader = new CSVReader(fileReader)) {
            String[] nextLine;
            var header = csvReader.readNext();
            var queryResultDtoBuilder = QueryResultDTO.builder();
            queryResultDtoBuilder.columns(Arrays.stream(header)
                    .map(x -> QueryResultDTO.Column.builder()
                            .name(x)
                            .type("string")
                            .build())
                    .toList());
            List<List<Object>> records = new ArrayList<>();
            while ((nextLine = csvReader.readNext()) != null) {
                records.add(Arrays.asList(nextLine));
            }
            return queryResultDtoBuilder
                    .rows(records)
                    .build();
        } catch (CsvValidationException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
