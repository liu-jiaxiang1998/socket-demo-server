package com.example.socketdemo.entity;

import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

@Slf4j
public class ProjectProperties {
    public static Integer LOCAL_LISTEN_PORT;
    public static String LOCAL_CAPTURE_ROOT_PATH;
    public static String LOCAL_CAPTURE_CAMERA_PATH;
    public static String LOCAL_CAPTURE_INFRARED_PATH;
    public static String LOCAL_CAPTURE_VISION_LIGHT_PATH;

    public static String LOCAL_UPLOAD_ROOT_PATH;
    public static String LOCAL_UPLOAD_ZL_PATH;
    public static String LOCAL_UPLOAD_HW_PATH;
    public static String LOCAL_UPLOAD_KJG_PATH;
    public static String LOCAL_UPLOAD_ALL_ZL_PATH;
    public static String LOCAL_UPLOAD_VIDEO_PATH;

    public static String ZL_CAMERA_IP;
    public static Integer ZL_CAMERA_PORT;
    public static String INFRARED_URL;
    public static String PATIO_URL;


    static {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = ProjectProperties.class.getClassLoader().getResourceAsStream("application.yml")) {
            Map<String, Object> jsonData = yaml.load(inputStream);
            LOCAL_LISTEN_PORT = getProperty(jsonData, "project.local.listenPort");
            LOCAL_CAPTURE_ROOT_PATH = getProperty(jsonData, "project.local.capturePath.root");
            LOCAL_CAPTURE_CAMERA_PATH = getProperty(jsonData, "project.local.capturePath.zlCamera");
            LOCAL_CAPTURE_INFRARED_PATH = getProperty(jsonData, "project.local.capturePath.infrared");
            LOCAL_CAPTURE_VISION_LIGHT_PATH = getProperty(jsonData, "project.local.capturePath.visionLight");

            LOCAL_UPLOAD_ROOT_PATH = getProperty(jsonData, "project.local.uploadPath.root");
            LOCAL_UPLOAD_ZL_PATH = getProperty(jsonData, "project.local.uploadPath.ZL");
            LOCAL_UPLOAD_HW_PATH = getProperty(jsonData, "project.local.uploadPath.HW");
            LOCAL_UPLOAD_KJG_PATH = getProperty(jsonData, "project.local.uploadPath.KJG");
            LOCAL_UPLOAD_ALL_ZL_PATH = getProperty(jsonData, "project.local.uploadPath.allZL");
            LOCAL_UPLOAD_VIDEO_PATH = getProperty(jsonData, "project.local.uploadPath.video");

            ZL_CAMERA_IP = getProperty(jsonData, "project.zlCamera.ip");
            ZL_CAMERA_PORT = getProperty(jsonData, "project.zlCamera.port");

            INFRARED_URL = getProperty(jsonData, "project.infraredUrl");

            PATIO_URL = getProperty(jsonData, "project.patioUrl");

            log.info("项目初始属性加载完毕");
        } catch (Exception e) {
            log.error("项目初始属性加载出错！" + e.getMessage());
        }
    }


    // 递归方法来获取多层嵌套的属性值
    private static <T> T getProperty(Map<String, Object> data, String key) {
        String[] keys = key.split("\\.");
        Map<String, Object> current = data;
        for (int i = 0; i < keys.length - 1; i++) {
            current = (Map<String, Object>) current.get(keys[i]);
            if (current == null) {
                return null;
            }
        }
        return (T) current.get(keys[keys.length - 1]);
    }
}
