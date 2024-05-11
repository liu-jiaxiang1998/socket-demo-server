package com.example.socketdemo.communicate;

import com.example.socketdemo.entity.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * 监听来自小盒子的信息，并进行相应的处理！只需要接收信息！
 * date: 2024/5/7
 * author: ljx
 */
@Slf4j
public class GKJServerSocket implements Runnable {

    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    private ExecutorService mainProcessExecutorService = Executors.newFixedThreadPool(10);

    private int serverListenPort;
    private BlockingQueue<CameraCaptureCommand> cameraCaptureCommandQueue;

    private ConcurrentHashMap<Integer, WeightFrame> weightFrameMap;
    private ConcurrentHashMap<Integer, CameraCaptureResult> zlCameraCaptureResultMap;
    private ConcurrentHashMap<Integer, String> zlHWCameraCaptureResultMap;
    private ConcurrentHashMap<Integer, String> zlKJGCameraCaptureResultMap;

    public GKJServerSocket(Integer serverListenPort, ConcurrentHashMap<Integer, WeightFrame> weightFrameMap,
                           BlockingQueue<CameraCaptureCommand> cameraCaptureCommandQueue,
                           ConcurrentHashMap<Integer, CameraCaptureResult> zlCameraCaptureResultMap,
                           ConcurrentHashMap<Integer, String> zlHWCameraCaptureResultMap,
                           ConcurrentHashMap<Integer, String> zlKJGCameraCaptureResultMap) {
        this.serverListenPort = serverListenPort;
        this.weightFrameMap = weightFrameMap;
        this.cameraCaptureCommandQueue = cameraCaptureCommandQueue;
        this.zlCameraCaptureResultMap = zlCameraCaptureResultMap;
        this.zlHWCameraCaptureResultMap = zlHWCameraCaptureResultMap;
        this.zlKJGCameraCaptureResultMap = zlKJGCameraCaptureResultMap;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(serverListenPort);
            log.info("Server is listening on port " + serverListenPort);
        } catch (IOException e) {
            log.error("Server can/t listen on port " + serverListenPort);
            log.error(e.getMessage());
        }

        while (true) {
            /** 这里就是单个连接的情况！！要是能够接收多个Socket，下面的实现就有问题了！ */
            try (Socket socket = serverSocket.accept()) {
                log.info("建立来自小盒子的连接");
                socket.setKeepAlive(true);
                ObjectMapper objectMapper = new ObjectMapper();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                while (socket.isConnected() && !socket.isClosed()) {
                    try {
                        String receivedData;
                        if ((receivedData = in.readLine()) != null) {
                            log.info("收到来自小盒子的信息是 " + receivedData);
                            ToGKJMessage toGKJMessage = objectMapper.readValue(receivedData, ToGKJMessage.class);
                            if (toGKJMessage.getType() == ToGKJMessageType.CAPTURE_FRAME) {
                                /** 收到小盒子的抓拍帧 */
                                CameraCaptureCommand captureCommand = objectMapper.readValue(toGKJMessage.getContent(), CameraCaptureCommand.class);
                                cameraCaptureCommandQueue.put(captureCommand);

                                /** 异步调用红外相机抓拍！ */
                                Callable<String> infraredSocket = new InfraredSocket();
                                CompletableFuture<String> completableFutureHW = CompletableFuture.supplyAsync(() -> {
                                    try {
                                        return infraredSocket.call();
                                    } catch (Exception e) {
                                        log.error("调用红外相机出现问题！");
                                        throw new RuntimeException(e);
                                    }
                                }, executorService);
                                completableFutureHW.thenAccept(result -> {
                                    zlHWCameraCaptureResultMap.put(captureCommand.getId(), result);
                                });

                                /** 异步调用可见光相机抓拍！ */
                                Callable<String> visibleLightSocket = new VisibleLightSocket();
                                CompletableFuture<String> completableFutureKJG = CompletableFuture.supplyAsync(() -> {
                                    try {
                                        return visibleLightSocket.call();
                                    } catch (Exception e) {
                                        log.error("调用可见光相机出现问题！");
                                        throw new RuntimeException(e);
                                    }
                                }, executorService);
                                completableFutureKJG.thenAccept(result -> {
                                    zlKJGCameraCaptureResultMap.put(captureCommand.getId(), result);
                                });

                            } else if (toGKJMessage.getType() == ToGKJMessageType.WEIGHT_FRAME) {
                                /** 收到小盒子的称重帧 */
                                WeightFrame weightFrame = objectMapper.readValue(toGKJMessage.getContent(), WeightFrame.class);
                                weightFrameMap.put(weightFrame.getUuid(), weightFrame);
                            } else if (toGKJMessage.getType() == ToGKJMessageType.DETECT_FAIL_FRAME
                                    || toGKJMessage.getType() == ToGKJMessageType.DETECT_SUCCESS_FRAME) {
                                /** 收到小盒子的检测帧 */
                                Object[] arrays = objectMapper.readValue(toGKJMessage.getContent(), Object[].class);
                                mainProcessExecutorService.submit(new MainProcess(weightFrameMap, zlCameraCaptureResultMap,
                                        zlHWCameraCaptureResultMap, zlKJGCameraCaptureResultMap, arrays, toGKJMessage.getType()));
                            } else {
                                log.error("未知的消息类型，跳过！");
                            }
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                }
                log.info("和小盒子的连接已断开，重新连接！");
            } catch (Exception e) {
                log.error("工控机接收小盒子信息出错！", e);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    log.error(ex.getMessage());
                }
            }
        }


    }
}
