package com.example.socketdemo.communicate;

import com.example.socketdemo.entity.CameraCaptureCommand;
import com.example.socketdemo.entity.CameraCaptureResult;
import com.example.socketdemo.entity.FileType;
import com.example.socketdemo.utils.CommonUtil;
import com.example.socketdemo.utils.CrcUtil;
import com.example.socketdemo.utils.HexUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ljx
 * @description: 相机模块重新实现，本来是分成发送子线程和接收子线程，但是试了下，各种问题，尤其是 Connection Reset 问题，我是真的服了！！根本不知道出错在哪里！！
 * 实在没招了，只能取消原有的实现，改成放在一起的实现！！
 * @date 2024/5/11
 */
@Slf4j
public class CameraSocket implements Runnable {
    private String cameraAddress;
    private int cameraPort;

    private BlockingQueue<CameraCaptureCommand> cameraCaptureCommandQueue;
    private ConcurrentHashMap<Integer, CameraCaptureResult> zlCameraCaptureResultMap;

    public CameraSocket(String cameraAddress, int cameraPort, BlockingQueue<CameraCaptureCommand> cameraCaptureCommandQueue,
                        ConcurrentHashMap<Integer, CameraCaptureResult> zlCameraCaptureResultMap) {
        this.cameraAddress = cameraAddress;
        this.cameraPort = cameraPort;

        this.cameraCaptureCommandQueue = cameraCaptureCommandQueue;
        this.zlCameraCaptureResultMap = zlCameraCaptureResultMap;
    }

    @Override
    public void run() {
        while (true) {
            try (Socket socket = new Socket(cameraAddress, cameraPort)) {
                log.info("正脸相机连接成功！");
//                socket.setSoTimeout(5000);
                socket.setKeepAlive(true);

                OutputStream outputStream = null;
                InputStream inputStream = null;
                try {
                    inputStream = socket.getInputStream();
                    outputStream = socket.getOutputStream();
                } catch (IOException e) {
                    log.error("正脸相机获取流异常！");
                }
                while (socket.isConnected() && !socket.isClosed()) {
                    try {
                        /** 发送命令 */
                        CameraCaptureCommand captureCommand = cameraCaptureCommandQueue.take();
                        String command = CrcUtil.crcXmodem(CommonUtil.intToHexString(captureCommand.getId()), captureCommand.getLane().toString());
                        byte[] commandBytes = HexUtil.hexStringToByteArray(command);
                        outputStream.write(commandBytes);
                        outputStream.flush();

                        /** 接收命令 */
                        byte[] lengthData = new byte[7];
                        if (inputStream.read(lengthData) != -1) {
                            int packLength = Integer.parseInt(HexUtil.byteArrayToHexString(lengthData).substring(6, 14), 16);
                            byte[] dataBody;
                            if (packLength > 1024) {
                                dataBody = new byte[packLength + 7];
                                System.arraycopy(lengthData, 0, dataBody, 0, 7);
                                int remainingBytes = packLength;
                                int offset = 7;
                                while (remainingBytes > 0) {
                                    int bytesRead = inputStream.read(dataBody, offset, remainingBytes);
                                    remainingBytes -= bytesRead;
                                    offset += bytesRead;
                                }
                            } else {
                                dataBody = inputStream.readNBytes(packLength);
                            }
                            inputStream.readNBytes(3);

                            String data1 = HexUtil.byteArrayToHexString(dataBody);
                            int uuid = Integer.parseInt(data1.substring(14, 18), 16);
                            int year = Integer.parseInt(data1.substring(18, 22), 16);
                            int yue = Integer.parseInt(data1.substring(22, 24), 16);
                            int ri = Integer.parseInt(data1.substring(24, 26), 16);
                            int shi = Integer.parseInt(data1.substring(26, 28), 16);
                            int fen = Integer.parseInt(data1.substring(28, 30), 16);
                            int second = Integer.parseInt(data1.substring(30, 32), 16);
                            int millisecond = Integer.parseInt(data1.substring(32, 36), 16);
                            log.info("检测编号" + uuid + "    检测时间：" + year + "年" + yue + "月" + ri + "日" + shi + "点" + fen + "分" + second + "秒" + millisecond + "毫秒");
                            int laneNumber = Integer.parseInt(data1.substring(36, 38), 16);
                            int shibie = Integer.parseInt(data1.substring(38, 40), 16);
                            String color = new String(Arrays.copyOfRange(dataBody, 20, 23), StandardCharsets.UTF_8);
                            String licencePlate = new String(Arrays.copyOfRange(dataBody, 23, 39), StandardCharsets.UTF_8);
                            int tuxianggeshu = Integer.parseInt(data1.substring(78, 80), 16);

                            int index = 80;
                            int imgBytesLength = Integer.parseInt(data1.substring(index, index + 8), 16) * 2;
                            byte[] imgBytes = HexUtil.hexStringToByteArray(data1.substring(index + 8, index + 8 + imgBytesLength));

                            Path filePath = Paths.get(CommonUtil.getLocalCapturePath(FileType.ZL));
                            Files.write(filePath, imgBytes);

                            CameraCaptureResult cameraCaptureResult = new CameraCaptureResult();

                            cameraCaptureResult.setUuid(uuid);

                            cameraCaptureResult.setLane(laneNumber);
                            cameraCaptureResult.setDirection(-1);

                            cameraCaptureResult.setLeftImgPath(filePath.toAbsolutePath().toString());
                            cameraCaptureResult.setRightImgPath(filePath.toAbsolutePath().toString());
                            cameraCaptureResult.setImgName(filePath.getFileName().toString());
                            cameraCaptureResult.setLicencePlate(licencePlate);
                            cameraCaptureResult.setColor(color);
                            cameraCaptureResult.setSpeed(0f);
                            cameraCaptureResult.setLaneNumber(laneNumber);
                            cameraCaptureResult.setIsCompleted(true);
                            log.info("抓拍结果为 " + cameraCaptureResult);
                            zlCameraCaptureResultMap.put(uuid, cameraCaptureResult);
                        }
                    } catch (Exception e) {
                        log.error("正脸相机出现异常，断开重连！", e);
                        break;
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    log.error(ex.getMessage());
                }
            }
        }
    }

//    class SendThread extends Thread {
//        private Socket socket;
//        private BlockingQueue<CameraCaptureCommand> cameraCaptureCommandQueue;
//        private Logger logger;
//
//        SendThread(Socket socket, BlockingQueue<CameraCaptureCommand> cameraCaptureCommandQueue) {
//            this.socket = socket;
//            this.cameraCaptureCommandQueue = cameraCaptureCommandQueue;
//            logger = LoggerFactory.getLogger(SendThread.class);
//        }
//
//        @Override
//        public void run() {
//            OutputStream outputStream = null;
//            try {
//                outputStream = socket.getOutputStream();
//            } catch (IOException e) {
//                log.error("正脸相机发送流异常！");
//            }
//            while (socket.isConnected()) {
//                try {
//                    CameraCaptureCommand captureCommand = cameraCaptureCommandQueue.take();
////                    log.info("captureCommand " + captureCommand.toString());
//                    String command = CrcUtil.crcXmodem(CommonUtil.intToHexString(captureCommand.getId()), captureCommand.getLane().toString());
////                    log.info("command " + command);
//                    byte[] commandBytes = HexUtil.hexStringToByteArray(command);
//                    outputStream.write(commandBytes);
//                    outputStream.flush();
//                } catch (Exception e) {
//                    logger.error(e.getMessage());
//                }
//            }
//            log.info("正脸相机发送线程执行结束！");
//        }
//    }
//
//    class ReceiveThread extends Thread {
//        private Socket socket;
//        private ConcurrentHashMap<Integer, CameraCaptureResult> zlCameraCaptureResultMap;
//        private Logger logger;
//
//        ReceiveThread(Socket socket, ConcurrentHashMap<Integer, CameraCaptureResult> zlCameraCaptureResultMap) {
//            this.socket = socket;
//            this.zlCameraCaptureResultMap = zlCameraCaptureResultMap;
//            logger = LoggerFactory.getLogger(ReceiveThread.class);
//        }
//
//        @Override
//        public void run() {
//            InputStream inputStream = null;
//            try {
//                inputStream = socket.getInputStream();
//            } catch (IOException e) {
//                log.error("正脸相机读取流异常！");
//                return;
//            }
//            while (socket.isConnected() && !socket.isClosed()) {
//                try {
//                    log.info("和正脸相机的socket仍然正常连接。发送紧急帧！");
//                    socket.sendUrgentData(0xFF);
//                    byte[] lengthData = new byte[7];
//                    if (inputStream.read(lengthData) != -1) {
//                        long startTime = System.currentTimeMillis();
//                        int packLength = Integer.parseInt(HexUtil.byteArrayToHexString(lengthData).substring(6, 14), 16);
//                        byte[] dataBody;
//                        if (packLength > 1024) {
//                            dataBody = new byte[packLength + 7];
//                            System.arraycopy(lengthData, 0, dataBody, 0, 7);
//                            int remainingBytes = packLength;
//                            int offset = 7;
//                            while (remainingBytes > 0) {
//                                int bytesRead = inputStream.read(dataBody, offset, remainingBytes);
//                                remainingBytes -= bytesRead;
//                                offset += bytesRead;
//                            }
//                        } else {
//                            dataBody = inputStream.readNBytes(packLength);
//                        }
//                        inputStream.readNBytes(3);
//
//                        String data1 = HexUtil.byteArrayToHexString(dataBody);
//                        int uuid = Integer.parseInt(data1.substring(14, 18), 16);
//                        int year = Integer.parseInt(data1.substring(18, 22), 16);
//                        int yue = Integer.parseInt(data1.substring(22, 24), 16);
//                        int ri = Integer.parseInt(data1.substring(24, 26), 16);
//                        int shi = Integer.parseInt(data1.substring(26, 28), 16);
//                        int fen = Integer.parseInt(data1.substring(28, 30), 16);
//                        int second = Integer.parseInt(data1.substring(30, 32), 16);
//                        int millisecond = Integer.parseInt(data1.substring(32, 36), 16);
//                        logger.info("检测编号" + uuid + "    检测时间：" + year + "年" + yue + "月" + ri + "日" + shi + "点" + fen + "分" + second + "秒" + millisecond + "毫秒");
//                        int laneNumber = Integer.parseInt(data1.substring(36, 38), 16);
//                        int shibie = Integer.parseInt(data1.substring(38, 40), 16);
//                        String color = new String(Base64.getDecoder().decode(data1.substring(40, 46)), StandardCharsets.UTF_8);
//                        String licencePlate = data1.substring(46, 78);
//                        int tuxianggeshu = Integer.parseInt(data1.substring(78, 80), 16);
//
//                        int index = 80;
//                        int imgBytesLength = Integer.parseInt(data1.substring(index, index + 8), 16) * 2;
//                        byte[] imgBytes = HexUtil.hexStringToByteArray(data1.substring(index + 8, index + 8 + imgBytesLength));
//
//                        Path filePath = Paths.get(CommonUtil.getLocalCapturePath(FileType.ZL));
//                        Files.write(filePath, imgBytes);
//                        long endTime = System.currentTimeMillis();
////                    logger.info("抓拍耗时：" + (endTime - startTime) + "ms" + " 地址为 " + filePath.toAbsolutePath().toString());
//
//                        CameraCaptureResult cameraCaptureResult = new CameraCaptureResult();
//
//                        cameraCaptureResult.setUuid(uuid);
//                        cameraCaptureResult.setLane(laneNumber);
//                        cameraCaptureResult.setDirection(-1);
//                        cameraCaptureResult.setLeftImgPath(filePath.toAbsolutePath().toString());
//                        cameraCaptureResult.setRightImgPath(filePath.toAbsolutePath().toString());
//                        cameraCaptureResult.setImgName(filePath.getFileName().toString());
//                        cameraCaptureResult.setLicencePlate(licencePlate);
//                        cameraCaptureResult.setColor(color);
//                        cameraCaptureResult.setSpeed(0f);
//                        cameraCaptureResult.setLaneNumber(laneNumber);
//                        cameraCaptureResult.setIsCompleted(true);
//                        zlCameraCaptureResultMap.put(uuid, cameraCaptureResult);
//                        log.info("抓拍结果为 " + cameraCaptureResult);
//                    }
//                } catch (Exception e) {
//                    logger.error("正脸相机抓拍报错！", e);
//                    try {
//                        Thread.sleep(500);
//                    } catch (InterruptedException ex) {
//                        throw new RuntimeException(ex);
//                    }
//                }
//            }
//            log.info("正脸相机接收线程执行结束！");
//        }
//    }
}

