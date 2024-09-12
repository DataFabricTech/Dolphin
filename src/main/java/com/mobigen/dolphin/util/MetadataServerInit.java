package com.mobigen.dolphin.util;

import com.mobigen.dolphin.repository.openmetadata.OpenMetadataRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Created by jblim
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
public class MetadataServerInit {
    private final OpenMetadataRepository omRepository;

    @PostConstruct
    public void initDataEngineBot() {
        omRepository.initBot();
    }
}
