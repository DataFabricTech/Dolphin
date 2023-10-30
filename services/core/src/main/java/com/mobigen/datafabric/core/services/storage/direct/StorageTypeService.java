package com.mobigen.datafabric.core.services.storage.direct;

import com.google.protobuf.ByteString;
import com.mobigen.datafabric.core.model.DataStorageTypeTable;
import com.mobigen.libs.grpc.Storage.StorageTypeModel;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.mobigen.datafabric.core.util.DataLayerConnection.getDataDB;
import static com.mobigen.sqlgen.SqlBuilder.select;


/**
 * StorageType 서비스
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 * @deprecated
 */
@Slf4j
@Deprecated
public class StorageTypeService {
    DataStorageTypeTable table = new DataStorageTypeTable();

    public List<StorageTypeModel> getStorageTypeModels() {
        var sql = select(table.getNameCol(), table.getIconCol())
                .from(table.getTable())
                .generate()
                .getStatement();
        log.info("sql: " + sql);
        var res = getDataDB(sql);
        List<StorageTypeModel> result = new ArrayList<>();
        try {
            while (res.next()) {
                result.add(StorageTypeModel.newBuilder()
                        .setName(res.getString(1))
                        .setIcon(ByteString.copyFrom(res.getBytes(2)))
                        .build());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

//    public StorageTypeModel createStorageType(String name) {
//        var sql = insert(table)
//                .columns(nameCol)
//                .values(name)
//                .generate()
//                .getStatement();
//        var resOfDataLayer = getData(sql);
//        var row = resOfDataLayer.getRows(0);
//        return StorageTypeModel.newBuilder()
//                .setId(row.getRow(0).getStringValue())
//                .setName(row.getRow(1).getStringValue())
//                .build();
//    }

}