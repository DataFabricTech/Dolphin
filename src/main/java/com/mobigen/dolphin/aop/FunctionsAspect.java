package com.mobigen.dolphin.aop;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobigen.dolphin.exception.DolphinException;
import com.mobigen.dolphin.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
@Component
@Aspect
public class FunctionsAspect {
    @Around("@annotation(WriteAroundLogging)")
    public Object writeLogging(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("Start {} function ...", joinPoint.getSignature().getName());
        try {
            return joinPoint.proceed();
        } finally {
            log.info("End {} function", joinPoint.getSignature().getName());
        }
    }

    @Around("@annotation(CheckOpenMetadata)")
    public Object checkOpenMetadata(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                var om = new ObjectMapper();
                var data = om.readValue(e.getResponseBodyAsString(), new TypeReference<Map<String, Object>>() {
                });
                throw new DolphinException(ErrorCode.NON_EXISTENT_MODEL, data.getOrDefault("message", "").toString());
            }
            throw e;
        }
    }
}
