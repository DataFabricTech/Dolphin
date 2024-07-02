package com.mobigen.dolphin.util;

import com.mobigen.dolphin.dto.response.QueryResultDTO;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
public class CsvDeSerializer {
    public static QueryResultDTO readCsv(String directoryPath) {
        var parser = new JSONParser();
        try (var dataReader = new CSVReader(new FileReader(directoryPath + "/data.csv"));
             var schemaReader = new FileReader(directoryPath + "/schema.json")
        ) {
            var jsonObject = (JSONObject) parser.parse(schemaReader);
            String[] nextLine;
            var queryResultDtoBuilder = QueryResultDTO.builder();
            List<QueryResultDTO.Column> columns = new ArrayList<>();
            List<String> columnNames = new ArrayList<>();
            Arrays.stream(dataReader.readNext()).forEach(x -> {
                columns.add(QueryResultDTO.Column.builder()
                        .name(x)
                        .dataType(DolphinType.fromValue((String) jsonObject.get(x)))
                        .build());
                columnNames.add(x);
            });
            List<List<Object>> records = new ArrayList<>();
            while ((nextLine = dataReader.readNext()) != null) {
                List<Object> record = new ArrayList<>();
                for (int i = 0; i < nextLine.length; i++) {
                    record.add(Functions.convertType(nextLine[i], columns.get(i).getDataType()));
                }
                records.add(record);
            }

            return queryResultDtoBuilder
                    .columns(columns)
                    .resultData(QueryResultDTO.ResultData.builder()
                            .columns(columnNames)
                            .rows(records)
                            .build())
                    .build();
        } catch (CsvValidationException | IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
