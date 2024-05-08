package com.example.socketdemo.communicate;

import com.example.socketdemo.entity.FileType;
import com.example.socketdemo.entity.ProjectProperties;
import com.example.socketdemo.utils.CommonUtil;
import com.example.socketdemo.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

/**
 * 工控机相连的红外相机
 * date: 2024/5/7
 * author: ljx
 */
@Slf4j
public class InfraredSocket implements Callable<String> {
    @Override
    public String call() throws Exception {
        Path path = Paths.get(CommonUtil.getLocalCapturePath(FileType.ZL_HW));
        String imgPath = path.toAbsolutePath().toString();
        try {
            byte[] bytes = HttpUtil.doGet(ProjectProperties.INFRARED_URL, null).bodyBytes();
            Files.write(path, bytes);
        } catch (IOException e) {
            log.error("红外图片写入错误！！");
        }
        return imgPath;
    }
}
