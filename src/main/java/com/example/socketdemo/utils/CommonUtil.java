package com.example.socketdemo.utils;

import com.example.socketdemo.entity.FileType;
import com.example.socketdemo.entity.ProjectProperties;

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
        return uploadRootPath.append(FILE_SEPARATOR).append(CommonUtil.formatFileName(new Date())).append(".jpg").toString();
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
        uploadRootPath.append(FILE_SEPARATOR);
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
                .append(localPath.substring(localPath.lastIndexOf(FILE_SEPARATOR))).toString();
    }
}
