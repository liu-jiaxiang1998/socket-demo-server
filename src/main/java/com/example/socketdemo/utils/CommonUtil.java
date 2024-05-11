package com.example.socketdemo.utils;

import com.example.socketdemo.entity.FileType;
import com.example.socketdemo.entity.ProjectProperties;
import com.example.socketdemo.entity.ToPatioMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class CommonUtil {
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");

    public static String formatFileName(Date currentTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS");
        return dateFormat.format(currentTime);
    }

    public static String getLocalCapturePath(FileType type) {
        StringBuilder uploadRootPath = new StringBuilder();
        uploadRootPath.append(System.getProperty("user.dir")).append(FILE_SEPARATOR).append(ProjectProperties.LOCAL_CAPTURE_ROOT_PATH).append(FILE_SEPARATOR);
        switch (type) {
            case ZL:
                uploadRootPath.append(ProjectProperties.LOCAL_CAPTURE_CAMERA_PATH);
                break;
            case ZL_HW:
                uploadRootPath.append(ProjectProperties.LOCAL_CAPTURE_INFRARED_PATH);
                break;
            case ZL_KJG:
                uploadRootPath.append(ProjectProperties.LOCAL_CAPTURE_VISION_LIGHT_PATH);
                break;
        }

        File directory = new File(uploadRootPath.toString());
        if (!directory.exists()) directory.mkdirs();
        if (type == FileType.ZL) {
            return uploadRootPath.append(FILE_SEPARATOR).append(CommonUtil.formatFileName(new Date())).append("_1_zl").append(".jpg").toString();
        } else {
            return uploadRootPath.append(FILE_SEPARATOR).append(CommonUtil.formatFileName(new Date())).append(".jpg").toString();
        }
    }

    public static String getLocalUploadPath(FileType type, String localPath) {
        StringBuilder uploadRootPath = new StringBuilder();
        uploadRootPath.append(ProjectProperties.LOCAL_UPLOAD_ROOT_PATH).append(FILE_SEPARATOR);
        switch (type) {
            case ZL:
                uploadRootPath.append(ProjectProperties.LOCAL_UPLOAD_ZL_PATH);
                break;
            case ZL_HW:
                uploadRootPath.append(ProjectProperties.LOCAL_UPLOAD_HW_PATH);
                break;
            case ZL_KJG:
                uploadRootPath.append(ProjectProperties.LOCAL_UPLOAD_KJG_PATH);
                break;
            case ALL_ZL:
                uploadRootPath.append(ProjectProperties.LOCAL_UPLOAD_ALL_ZL_PATH);
                break;
        }
        String timeStr = localPath.substring(localPath.lastIndexOf(FILE_SEPARATOR), localPath.lastIndexOf("."));
        List<String> stringList = Arrays.stream(timeStr.split("_")).map(a -> {
            if (a.startsWith("0")) {
                return a.substring(1, a.length());
            } else {
                return a;
            }
        }).collect(Collectors.toList());
        return uploadRootPath.append(stringList.get(0)).append(FILE_SEPARATOR)
                .append(stringList.get(1)).append(FILE_SEPARATOR)
                .append(stringList.get(2)).append(FILE_SEPARATOR)
                .append(stringList.get(3)).append(FILE_SEPARATOR)
                .append(localPath.substring(localPath.lastIndexOf(FILE_SEPARATOR) + 1)).toString();
    }

    /**
     * 将 Int 转为两个字节的16进制字符串！
     */
    public static String intToHexString(int num) {
        // 将整数按位与运算获取最低8位
        int lowerByte = num & 0xFF;
        // 将整数右移8位再按位与运算获取次低8位
        int upperByte = (num >> 8) & 0xFF;
        // 将两个字节的整数格式化为十六进制字符串
        String hexString = String.format("%02X %02X", upperByte, lowerByte);
        return hexString;
    }

    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        ToPatioMessage toPatioMessage = new ToPatioMessage();
        try {
            System.out.println(objectMapper.writeValueAsString(toPatioMessage));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
