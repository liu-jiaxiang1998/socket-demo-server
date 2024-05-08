package com.example.socketdemo;

import com.example.socketdemo.communicate.CameraSocket;
import com.example.socketdemo.communicate.GKJServerSocket;
import com.example.socketdemo.entity.CameraCaptureCommand;
import com.example.socketdemo.entity.CameraCaptureResult;
import com.example.socketdemo.entity.ProjectProperties;
import com.example.socketdemo.entity.WeightFrame;
import com.example.socketdemo.utils.SQLUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@SpringBootApplication
@Slf4j
public class SocketDemoApplication implements CommandLineRunner {

//    @Autowired
//    private SocketServerService socketServerService;

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SocketDemoApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
    }


    @Override
    public void run(String... args) throws Exception {
//        System.out.println("传递的参数为：\n");
//        Arrays.stream(args).forEach(arg -> System.out.print(arg + " "));
//        System.out.println();
//        socketServerService.serve();
        BlockingQueue<CameraCaptureCommand> cameraCaptureCommandQueue = new LinkedBlockingQueue<>();
        ConcurrentHashMap<Integer, CameraCaptureResult> zlCameraCaptureResultMap = new ConcurrentHashMap<>();
        ConcurrentHashMap<Integer, WeightFrame> weightFrameMap = new ConcurrentHashMap<>();
        ConcurrentHashMap<Integer, String> zlHWCameraCaptureResultMap = new ConcurrentHashMap<>();
        ConcurrentHashMap<Integer, String> zlKJGCameraCaptureResultMap = new ConcurrentHashMap<>();

        SQLUtil.init();

        new Thread(new CameraSocket(ProjectProperties.ZL_CAMERA_IP, ProjectProperties.ZL_CAMERA_PORT, cameraCaptureCommandQueue, zlCameraCaptureResultMap)).start();
        log.info("正脸相机抓拍线程已启动！");
        new Thread(new GKJServerSocket(ProjectProperties.LOCAL_LISTEN_PORT, weightFrameMap, cameraCaptureCommandQueue
                , zlCameraCaptureResultMap, zlHWCameraCaptureResultMap, zlKJGCameraCaptureResultMap)).start();
        log.info("接收小盒子信息线程已启动！");

    }

}
