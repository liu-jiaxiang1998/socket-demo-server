package com.example.socketdemo.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Http请求工具类
 * 测试通过，可以正常使用！
 * date: 2024/5/7
 * author: ljx
 */
@Slf4j
public class HttpUtil {

    public static HttpResponse doGet(String url, Map<String, String> headers) {
        HttpRequest httpRequest = HttpRequest.get(url);
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpRequest.header(header.getKey(), header.getValue());
            }
        }
        return httpRequest.execute();
    }

    public static String doPost(String url, Object data, Map<String, String> headers) {
        ObjectMapper mapper = new ObjectMapper();
        String dataJson = null;
        try {
            dataJson = mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("参数序列化失败");
        }
        return doPost(url, dataJson, headers);
    }


    public static String doPost(String url, Map<String, Object> data, Map<String, String> headers) {
        ObjectMapper mapper = new ObjectMapper();
        String dataJson = null;
        try {
            dataJson = mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("参数序列化失败");
        }
        return doPost(url, dataJson, headers);
    }

    public static String doPost(String url, String jsonBody, Map<String, String> headers) {
        HttpRequest post = HttpRequest.post(url);
        post.setConnectionTimeout(3000);
        post.setReadTimeout(5000);
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                post.header(header.getKey(), header.getValue());
            }
        }
        HttpResponse httpResponse = post.body(jsonBody).execute();
        return httpResponse.body();
    }

    public static String doPut(String url, Map<String, Object> data, Map<String, String> headers) {
        ObjectMapper mapper = new ObjectMapper();
        String dataJson = null;
        try {
            dataJson = mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("参数序列化失败");
        }
        HttpRequest put = HttpRequest.put(url);
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                put.header(header.getKey(), header.getValue());
            }
        }
        HttpResponse httpResponse = put.body(dataJson).execute();
        return httpResponse.body();
    }
}
