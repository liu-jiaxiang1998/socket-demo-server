package com.example.socketdemo.communicate;

import com.example.socketdemo.entity.*;
import com.example.socketdemo.utils.CommonUtil;
import com.example.socketdemo.utils.HttpUtil;
import com.example.socketdemo.utils.SQLUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工控机的主体处理流程。以线程池的方式运行。
 * date: 2024/5/7
 * author: ljx
 */
@Slf4j
public class MainProcess implements Runnable {

    private ConcurrentHashMap<Integer, WeightFrame> weightFrameMap;
    private ConcurrentHashMap<Integer, CameraCaptureResult> zlCameraCaptureResultMap;
    private ConcurrentHashMap<Integer, String> zlHWCameraCaptureResultMap;
    private ConcurrentHashMap<Integer, String> zlKJGCameraCaptureResultMap;

    private Object[] arrays;
    private ToGKJMessageType type;

    public MainProcess(ConcurrentHashMap<Integer, WeightFrame> weightFrameMap,
                       ConcurrentHashMap<Integer, CameraCaptureResult> zlCameraCaptureResultMap,
                       ConcurrentHashMap<Integer, String> zlHWCameraCaptureResultMap,
                       ConcurrentHashMap<Integer, String> zlKJGCameraCaptureResultMap,
                       Object[] arrays,
                       ToGKJMessageType type) {
        this.weightFrameMap = weightFrameMap;
        this.zlCameraCaptureResultMap = zlCameraCaptureResultMap;
        this.zlHWCameraCaptureResultMap = zlHWCameraCaptureResultMap;
        this.zlKJGCameraCaptureResultMap = zlKJGCameraCaptureResultMap;
        this.arrays = arrays;
        this.type = type;
    }

    @Override
    public void run() {
        try {
            int uuid = type == ToGKJMessageType.DETECT_FAIL_FRAME ? (Integer) arrays[1] : (Integer) arrays[0];

            /** 获取uuid对应的正脸相机抓拍结果！ */
            CameraCaptureResult cameraCaptureResult;
            int count = 0;
            while ((cameraCaptureResult = zlCameraCaptureResultMap.get(uuid)) == null && count++ < 5)
                Thread.sleep(1000);
            zlCameraCaptureResultMap.remove(uuid);
            if (cameraCaptureResult == null) {
                log.error("获取不到正脸相机抓拍的图片！");
                return;
            }
            /** 获取uuid对应的称重帧 */
            WeightFrame weightFrame;
            count = 0;
            while ((weightFrame = weightFrameMap.get(uuid)) == null && count++ < 5) Thread.sleep(1000);
            weightFrameMap.remove(uuid);
            if (weightFrame == null) {
                log.error("睡眠3s仍然获取不到uuid为 " + uuid + " 的称重帧！放弃本条记录！");
                return;
            }

            if (type == ToGKJMessageType.DETECT_FAIL_FRAME) {
                /** 收到小盒子的检测失败帧 */
                int status = (Integer) arrays[0];
                int lane = (Integer) arrays[2];
                Integer visionWeight = (Integer) arrays[3];
                String ceImgName = (String) arrays[arrays.length - 1];
                String ceImgAbsolutePath = (String) arrays[arrays.length - 2];
                if (status == 0 || status == -1) {
                    String leftImgPath = cameraCaptureResult.getLeftImgPath();
                    String zlImgAbsolutePath = CommonUtil.getLocalUploadPath(FileType.ALL_ZL, leftImgPath);
                    while (!Files.exists(Paths.get(leftImgPath))) {
                        log.info("正脸抓拍图片还未获取到，sleep 1s!!");
                        Thread.sleep(1000);
                    }
//                    Files.createFile(Paths.get(zlImgAbsolutePath));
                    Files.createDirectories(Paths.get(zlImgAbsolutePath).getParent());
                    Files.move(Paths.get(leftImgPath), Paths.get(zlImgAbsolutePath));

                    String zlHWImgCapturePath = zlHWCameraCaptureResultMap.remove(uuid);
                    String zlKJGImgCapturePath = zlKJGCameraCaptureResultMap.remove(uuid);
                    try {
                        new File(zlHWImgCapturePath).delete();
                        new File(zlKJGImgCapturePath).delete();
                    } catch (Exception e) {
                        log.error("删除红外和可见光相机字典出错！");
                    }

                    if (weightFrame.getWeight() != 0 && weightFrame.getWeight() <= 4.0) {
                        log.info("小汽车过车数据准备发送给绕行平台");
                        ToPatioMessage toPatioMessage = new ToPatioMessage()
                                .setDeviceKey("111").setIndexCode("smallCar").setVihiclePoint(3).setName(ceImgName)
                                .setNamefront(cameraCaptureResult.getImgName() + "_front").setChedaohao(lane)
                                .setPasstime(cameraCaptureResult.getImgName().substring(0, cameraCaptureResult.getImgName().lastIndexOf(".")))
                                .setChepai(cameraCaptureResult.getLicencePlate().trim())
                                .setZhoushu(0).setZhouzuluntai(" ").setWeight(visionWeight.doubleValue()).setManzailv(0d)
                                .setChechang(0d).setChekuan(0d).setChegao(0d).setXiangchang(0d).setXiangkuan(0d).setXianggao(0d).setLidigao(0d).setLanbanchang(0d).setLanbangao(0d)
                                .setManhuo(0).setCzHuowu(-1).setChengzhong(weightFrame.getWeight().doubleValue())
                                .setPictureHead(" ").setPictureTail(" ").setPictureCrop(" ").setPictureZl(zlImgAbsolutePath).setPictureCl(ceImgAbsolutePath)
                                .setVideo("----").setFugai(100).setCorpCoordinate(" ").setHeadCoordinate(" ").setTyreCoordinate(" ")
                                .setSpeed(weightFrame.getSpeed());

                        HttpUtil.doPost(ProjectProperties.PATIO_URL, toPatioMessage, Map.of("Content-type", "application/json"));
                        log.info("小汽车过车数据发送给绕行平台成功！");
                    } else {
                        log.warn("小汽车地磅称重数据有问题，不发送！地磅称重为 " + weightFrame.getWeight() + "t");
                    }
                }
            } else if (type == ToGKJMessageType.DETECT_SUCCESS_FRAME) {
                /** 收到小盒子的检测成功帧 */
                log.info("uuid为 " + uuid + " 的图片货车检测成功");
//                int i = 0;
//                for (Object obj : arrays) {
//                    log.info( i++ + " " + obj.getClass().getName());
//                }
                String ceImgName = (String) arrays[arrays.length - 1];
                String ceImgAbsolutePath = (String) arrays[arrays.length - 2];
                String ceImgAbsolutePath2 = (String) arrays[arrays.length - 3];
                String ceHeadImgAbsolutePath = (String) arrays[arrays.length - 4];
                String ceCropImgAbsolutePath = (String) arrays[arrays.length - 5];

                String zlHWImgCapturePath = zlHWCameraCaptureResultMap.remove(uuid);
                String zlKJGImgCapturePath = zlKJGCameraCaptureResultMap.remove(uuid);
                if (zlHWImgCapturePath != null) {
                    try {
                        String zlHWImgAbsolutePath = CommonUtil.getLocalUploadPath(FileType.ZL_HW, zlHWImgCapturePath);
                        Files.createDirectories(Paths.get(zlHWImgAbsolutePath).getParent());
                        Files.move(Paths.get(zlHWImgCapturePath), Paths.get(zlHWImgAbsolutePath));
                    } catch (Exception e) {
                        log.error("转移工控机红外图片出错");
                    }
                }
                if (zlKJGImgCapturePath != null) {
                    try {
                        String zlKJGImgAbsolutePath = CommonUtil.getLocalUploadPath(FileType.ZL_KJG, zlKJGImgCapturePath);
                        Files.createDirectories(Paths.get(zlKJGImgAbsolutePath).getParent());
                        Files.move(Paths.get(zlKJGImgCapturePath), Paths.get(zlKJGImgAbsolutePath));
                    } catch (Exception e) {
                        log.error("转移工控机可见光图片出错");
                    }
                }

                String zlImgName = cameraCaptureResult.getImgName();
                String[] zlImgNames = zlImgName.split("_");
                String leftImgPath = cameraCaptureResult.getLeftImgPath();
                String zlImgAbsolutePath = CommonUtil.getLocalUploadPath(FileType.ZL, leftImgPath);

                while (!Files.exists(Paths.get(leftImgPath))) {
                    log.info("正脸抓拍图片还未获取到，sleep 1s!!");
                    Thread.sleep(1000);
                }
//                Files.createFile(Paths.get(zlImgAbsolutePath));
                Files.createDirectories(Paths.get(zlImgAbsolutePath).getParent());
                Files.move(Paths.get(leftImgPath), Paths.get(zlImgAbsolutePath));

                String timeStr = cameraCaptureResult.getImgName().substring(0, cameraCaptureResult.getImgName().lastIndexOf("_1")).replace("_", "");
                String picture_name = cameraCaptureResult.getImgName().substring(0, cameraCaptureResult.getImgName().lastIndexOf("_"));
                String imgNameFront = timeStr + "_front";
                String[] zhou1 = {String.valueOf(arrays[8]), String.valueOf(arrays[9]), String.valueOf(arrays[10]), String.valueOf(arrays[11]), String.valueOf(arrays[12]), String.valueOf(arrays[13])};
                String[] zhou2 = {String.valueOf(arrays[14]), String.valueOf(arrays[15]), String.valueOf(arrays[16]), String.valueOf(arrays[17]), String.valueOf(arrays[18]), String.valueOf(arrays[19])};

                String zhouzuxihao = String.join(",", zhou1);
                String zhouzuluntai = String.join(",", zhou2);

                if (weightFrame.getWeight() != 0) {
                    SQLUtil.executeSpecialUpdateInTransaction(zlImgNames[0], zlImgNames[1], zlImgNames[2], zlImgNames[3], zlImgNames[4],
                            timeStr, picture_name, imgNameFront, String.valueOf(0), cameraCaptureResult.getColor().trim(),
                            String.valueOf(weightFrame.getSpeed()), cameraCaptureResult.getLicencePlate().trim(), String.valueOf(cameraCaptureResult.getLaneNumber()),
                            String.valueOf(arrays[6]), String.valueOf(arrays[7]), zhouzuxihao, zhouzuluntai, String.valueOf(arrays[20]),
                            String.valueOf(arrays[21]), String.valueOf(arrays[22]), String.valueOf(arrays[23]), String.valueOf(0),
                            String.valueOf(arrays[24]), String.valueOf(arrays[25]), String.valueOf(arrays[41]), String.valueOf(arrays[42]),
                            String.valueOf(arrays[26]), String.valueOf(0), String.valueOf(arrays[38]),
                            String.valueOf(arrays[39]), String.valueOf(arrays[40]), String.valueOf(arrays[27]),
                            String.valueOf(arrays[34]), String.valueOf(arrays[43]), String.valueOf(1), String.valueOf(0),
                            String.valueOf(0),
                            String.valueOf(0), String.valueOf(0),
                            String.valueOf(arrays[35]), String.valueOf(arrays[36]), String.valueOf(arrays[37]),
                            String.valueOf(weightFrame.getWeight()), String.valueOf(arrays[32]), String.valueOf(arrays[44]), String.valueOf(arrays[45]), String.valueOf(arrays[46]),
                            zlImgAbsolutePath, ceImgAbsolutePath, ceCropImgAbsolutePath, ceHeadImgAbsolutePath, " "
                    );
                    log.info(" uuid为 " + uuid + " 的数据成功插入数据库！");

                    if (weightFrame.getWeight() != 0 && weightFrame.getWeight() > 2.0) {
                        ToPatioMessage toPatioMessage = new ToPatioMessage()
                                .setDeviceKey("111").setIndexCode("2222").setVihiclePoint(3).setName(ceImgName)
                                .setNamefront(imgNameFront).setChedaohao((Integer) arrays[2])
                                .setPasstime(zlImgName.substring(0, zlImgName.lastIndexOf(".")))
                                .setChepai(cameraCaptureResult.getLicencePlate().trim())
                                .setZhoushu((Integer) arrays[6]).setZhouzuluntai(zhouzuluntai).setWeight(Double.valueOf(arrays[32].toString())).setManzailv(Double.valueOf(arrays[26].toString()))
                                .setChechang(Double.valueOf(arrays[20].toString())).setChekuan(Double.valueOf(arrays[21].toString()))
                                .setChegao(Double.valueOf(arrays[22].toString())).setXiangchang(Double.valueOf(arrays[23].toString()))
                                .setXiangkuan(0d).setXianggao(Double.valueOf(arrays[24].toString())).setLidigao(Double.valueOf(arrays[25].toString()))
                                .setLanbanchang(Double.valueOf(arrays[41].toString())).setLanbangao(Double.valueOf(arrays[42].toString()))
                                .setManhuo((Integer) arrays[34]).setCzHuowu((Integer) arrays[43]).setChengzhong(weightFrame.getWeight().doubleValue())
                                .setPictureHead(ceHeadImgAbsolutePath).setPictureTail(" ").setPictureCrop(ceCropImgAbsolutePath).setPictureZl(zlImgAbsolutePath).setPictureCl(ceImgAbsolutePath)
                                .setVideo("----").setFugai((Integer) arrays[27]).setCorpCoordinate((String) arrays[44]).setHeadCoordinate((String) arrays[45]).setTyreCoordinate((String) arrays[46])
                                .setSpeed(weightFrame.getSpeed());

                        HttpUtil.doPost(ProjectProperties.PATIO_URL, toPatioMessage, Map.of("Content-type", "application/json"));
                        log.info(" uuid为 " + uuid + " 的数据成功发送到绕行平台！");
                    } else {
                        log.warn("地磅称重：" + weightFrame.getWeight() + "t 小于等于 2.0t,不能发送给绕行平台服务器");
                    }
                }
            }
        } catch (Exception e) {
            log.error("工控机主体流程出错！", e);
        }
    }
}
