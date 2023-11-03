package com.mobigen.datafabric.core.controller;

import com.mobigen.datafabric.core.services.storage.DataStorageService;
import com.mobigen.datafabric.core.util.JdbcConnector;
import com.mobigen.datafabric.share.protobuf.StorageOuterClass;
import com.mobigen.datafabric.share.protobuf.Utilities;
import com.mobigen.libs.grpc.StorageServiceCallBack;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.mobigen.datafabric.core.util.DataLayerUtilFunction.convertInputField;

/**
 * gRPC 의 request 를 받아 response 를 생성하는 콜백 클래스의 구현부
 * Storage 관련 서비스 제공
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
public class StorageServiceImpl implements StorageServiceCallBack {
    DataStorageService dataStorageService = new DataStorageService();

    @Override
    public StorageOuterClass.ResStorageOverview overview() {
        return StorageOuterClass.ResStorageOverview.newBuilder()
                .setCode("OK")
                .setData(StorageOuterClass.ResStorageOverview.Data.newBuilder()
                        .addAllStorageTypeCount(List.of(
                                StorageOuterClass.StorageTypeCount.newBuilder()
                                        .setCount(4)
                                        .setStorageType("postgresql")
                                        .build()
                        ))
                        .addAllStorageStatusCount(List.of(
                                StorageOuterClass.StorageStatusCount.newBuilder()
                                        .setCount(5)
                                        .setStatus(1)
                                        .build()
                        ))
                        .addAllStorageStatistics(List.of(
                                StorageOuterClass.StorageStatistics.newBuilder()
                                        .build()
                        ))
                        .build())
                .build();
    }

    @Override
    public StorageOuterClass.ResStorage search(StorageOuterClass.ReqStorageSearch request) {
        var filters = request.getFilter();
        var sorts = request.getSortsList();
        return StorageOuterClass.ResStorage.newBuilder()
                .setCode("OK")
                .setData(StorageOuterClass.ResStorage.Data.newBuilder()
                        .addAllStorage(dataStorageService.search())
                        .build())
                .build();
    }

    @Override
    public StorageOuterClass.ResStorage status(Utilities.ReqId request) {
        return StorageOuterClass.ResStorage.newBuilder()
                .setCode("OK")
                .setData(StorageOuterClass.ResStorage.Data.newBuilder()
                        .addAllStorage(List.of(dataStorageService.status(request.getId())))
                        .build())
                .build();
    }

    @Override
    public StorageOuterClass.ResStorage default_(Utilities.ReqId request) {
        return StorageOuterClass.ResStorage.newBuilder()
                .setCode("OK")
                .setData(StorageOuterClass.ResStorage.Data.newBuilder()
                        .addAllStorage(List.of(dataStorageService.getStorage(request.getId())))
                        .build())
                .build();
    }

    @Override
    public StorageOuterClass.ResStorage advanced(Utilities.ReqId request) {
        return StorageOuterClass.ResStorage.newBuilder()
                .setCode("OK")
                .setData(StorageOuterClass.ResStorage.Data.newBuilder()
                        .addAllStorage(List.of(dataStorageService.getStorage(request.getId())))
                        .build())
                .build();
    }

    @Override
    public StorageOuterClass.ResStorageBrowse browse() {
        return null;
    }

    @Override
    public StorageOuterClass.ResStorageBrowseDefault browseDefault() {
        return null;
    }

    @Override
    public Utilities.CommonResponse connectTest(StorageOuterClass.ConnInfo request) {
        Map<String, Object> basic = new HashMap<>();

        for (var op : request.getBasicOptionsList()) {
            basic.put(op.getKey().toLowerCase(), convertInputField(op));
        }

        Properties addition = new Properties();

        for (var op : request.getAdvancedOptionsList()) {
            addition.put(op.getKey().toLowerCase(), convertInputField(op));
        }

        var urlFormat = request.getUrlFormat();
        try (var connector = new JdbcConnector(urlFormat, basic)) {
            var conn = connector.connect(addition);
            var cur = conn.cursor();
            cur.execute("select 1");
            var result = cur.getResultSet();
            System.out.println(result);
            result.next();
            var value = result.getString(1);
            if (value.equals("1")) {
                return Utilities.CommonResponse.newBuilder().setCode("OK").build();
            } else {
                return Utilities.CommonResponse.newBuilder().setCode("FAIL").build();
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            return Utilities.CommonResponse.newBuilder()
                    .setCode("FAIL")
                    .setErrMsg(e.getMessage())
                    .build();
        }
    }

    @Override
    public Utilities.CommonResponse addStorage(StorageOuterClass.Storage request) {
        try {
            dataStorageService.addStorage(request);
            return Utilities.CommonResponse.newBuilder()
                    .setCode("OK")
                    .build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Utilities.CommonResponse.newBuilder()
                    .setCode("FAIL")
                    .setErrMsg(e.getMessage())
                    .build();
        }
    }

    @Override
    public Utilities.CommonResponse updateStorage() {
        return null;
    }

    @Override
    public StorageOuterClass.ResConnectedData connectedData() {
        return null;
    }

    @Override
    public Utilities.CommonResponse deleteStorage(Utilities.ReqId request) {
        try {
            dataStorageService.deleteStorage(request.getId());
            return Utilities.CommonResponse.newBuilder()
                    .setCode("OK")
                    .build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Utilities.CommonResponse.newBuilder()
                    .setCode("FAIL")
                    .setErrMsg(e.getMessage())
                    .build();
        }
    }
}
