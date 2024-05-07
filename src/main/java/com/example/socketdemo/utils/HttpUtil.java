package com.example.socketdemo.utils;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
* Http请求工具类
 * TODO 可以测试图书管理系统！
* date: 2024/5/7
* author: ljx
*/
public class HttpUtil {
        public static String doGet(String url) {
            // 发送GET请求并获取响应
            HttpResponse response = HttpRequest.get(url).execute();
            // 返回响应内容
            return response.body();
        }

        public static String doPost(String url, String data) {
            // 发送POST请求并获取响应
            HttpResponse response = HttpRequest.post(url).body(data).execute();
            // 返回响应内容
            return response.body();
        }

    public static String doPost(String url, Map<String, Object> data, Map<String, String> headers) {
        // 转换数据为JSON格式
        ObjectMapper mapper = new ObjectMapper();
        String dataJson = null;
        try {
            dataJson = mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        // 发送POST请求并获取响应
        HttpResponse response = HttpRequest.post(url)
                .header("Content-Type", "application/json")
                .body(dataJson)
                .execute();

        // 返回响应内容
        return response.body();
    }

    public static void main(String[] args) {
        // 请求URL
        String url = "http://10.70.123.228:7080/carsInfo/add";

        // 请求数据
        Map<String, Object> data = Map.of(
                "deviceKey", "111",
                "indexCode", "smallCar"
                // 其他请求参数...
        );

        // 请求头
        Map<String, String> headers = Map.of(
                "Content-Type", "application/json"
        );

        // 发送POST请求
        String response = doPost(url, data, headers);
        System.out.println("POST响应：" + response);
    }
}
