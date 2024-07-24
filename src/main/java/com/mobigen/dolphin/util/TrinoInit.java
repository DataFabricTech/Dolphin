package com.mobigen.dolphin.util;

import com.mobigen.dolphin.config.DolphinConfiguration;
import com.mobigen.dolphin.repository.trino.TrinoRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class TrinoInit {
    private final DolphinConfiguration dolphinConfiguration;
    private final TrinoRepository trinoRepository;

    @PostConstruct
    public void getOrCreateMetastoreCatalog() {
        var catalogs = trinoRepository.getCatalogs();
        var catalogName = dolphinConfiguration.getModel().getCatalog();
        boolean makeCatalog = true;
        for (var catalog : catalogs) {
            if (catalog.equals(catalogName)) {
                log.info("Already created trino catalog {}", catalogName);
                makeCatalog = false;
                break;
            }
        }
        if (makeCatalog) {
            trinoRepository.execute("create catalog " + catalogName
                    + " using hive"
                    + " with ("
                    + " \"hive.metastore.uri\" = '" + dolphinConfiguration.getHiveMetastore().getUri() + "'"
                    + ")");
        }
    }
}
