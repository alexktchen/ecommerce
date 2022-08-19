package com.kt.ecommerce.config;

import com.kt.ecommerce.dto.KTHttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class WebExceptionAdvice {

    @ExceptionHandler(RuntimeException.class)
    public KTHttpResponse handleRuntimeException(RuntimeException e) {
        log.error(e.toString(), e);
        return KTHttpResponse.fail("Server Error");
    }
}