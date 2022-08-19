package com.kt.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KTHttpResponse {
    private Boolean success;
    private String errorMsg;
    private Object data;
    private Long total;

    public static KTHttpResponse ok() {
        return new KTHttpResponse(true, null, null, null);
    }

    public static KTHttpResponse ok(Object data) {
        return new KTHttpResponse(true, null, data, null);
    }

    public static KTHttpResponse ok(List<?> data, Long total) {
        return new KTHttpResponse(true, null, data, total);
    }

    public static KTHttpResponse fail(String errorMsg) {
        return new KTHttpResponse(false, errorMsg, null, null);
    }
}
