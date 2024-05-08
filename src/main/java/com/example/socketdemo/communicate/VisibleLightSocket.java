package com.example.socketdemo.communicate;

import com.example.socketdemo.entity.FileType;
import com.example.socketdemo.utils.CommonUtil;
import com.example.socketdemo.utils.HttpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 工控机相连的可见光相机
 * 网络不通，代码正确性未知。。
 * date: 2024/5/7
 * author: ljx
 */
@Slf4j
public class VisibleLightSocket implements Callable<String> {
    @Override
    public String call() throws Exception {
        String url = "http://10.70.123.227/SDK/UNIV_API";

        HashMap<String, Object> param = new HashMap<>();
        param.put("session", 0);
        param.put("id", 1);
        param.put("call", Map.of(
                "service", "rpc",
                "method", "login"));
        param.put("params", Map.of(
                "userName", "admin",
                "password", "2ec72e25324f56a9885b399920fcd32b6fa72e7c4e62d12a45b37c03d280fd6e",
                "ip", "127.0.0.1",
                "random", "MHLFAK",
                "port", 80,
                "encryptType", 1));
        String response = HttpUtil.doPost(url, param, null);
        ObjectMapper mapper = new ObjectMapper();
        Map readValue1 = mapper.readValue(response, Map.class);
        String params = (String) readValue1.get("params");
        Map readValue2 = mapper.readValue(params, Map.class);
        String session = (String) readValue2.get("session");

        param.clear();
        param.put("session", session);
        param.put("id", 2);
        param.put("call", Map.of(
                "service", "rpc",
                "method", "keepAlive"));
        param.put("params", Map.of(
                "timeout", 60));
        HttpUtil.doPost(url, param, null);

        param.clear();
        param.put("session", session);
        param.put("id", 2);
        param.put("call", Map.of(
                "service", "snap",
                "method", "getSnapData"));
        param.put("params", Map.of(
                "quality", 50));
        response = HttpUtil.doPost(url, param, null);
        Map readValue3 = mapper.readValue(response, Map.class);
        String params2 = (String) readValue3.get("params");
        Map readValue4 = mapper.readValue(params2, Map.class);

        Path path = Paths.get(CommonUtil.getLocalCapturePath(FileType.ZL_KJG));
        String imgPath = path.toAbsolutePath().toString();
        try {
            Files.write(path, readValue4.get("Data").toString().getBytes());
        } catch (IOException e) {
            log.error("可见光图片写入错误！！");
        }
        return imgPath;
    }

}
