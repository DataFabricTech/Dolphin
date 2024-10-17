package com.mobigen.dolphin.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Created by jblim
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetadataServerInit {
    @Primary
    public void initDataEngineBot() {
        log.info("skip postConstruct method");
    }
}
