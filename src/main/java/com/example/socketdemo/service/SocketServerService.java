package com.example.socketdemo.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class SocketServerService {

    private static final Integer SEVER_PORT = 8999;
    private static final int THREAD_POOL_SIZE = 10;
    private static ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public void hello() {
        System.out.println("服务端程序运行！");
    }

    public void serve() {
        try (ServerSocket serverSocket = new ServerSocket(SEVER_PORT)) {
            System.out.println("服务器已启动，等待客户端连接...");

            while (true) {
//                Thread.sleep(60000);
                Socket clientSocket = serverSocket.accept();

                String remoteAddress = clientSocket.getInetAddress().getHostAddress();
                int remotePort = clientSocket.getPort();
                String localAddress = clientSocket.getLocalAddress().getHostAddress();
                int localPort = clientSocket.getLocalPort();
                boolean isConnected = clientSocket.isConnected();
                System.out.println("远程地址：" + remoteAddress + "，远程端口：" + remotePort +
                        "，本地地址：" + localAddress + "，本地端口：" + localPort +
                        "，是否连接：" + isConnected);

                // 将ExecutorService转换为ThreadPoolExecutor
                ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
                // 获取线程池的各种信息
                System.out.println("处理任务前线程池的状态：---------------------------------------------------");
                System.out.println("核心线程数量：" + threadPoolExecutor.getCorePoolSize());
                System.out.println("最大线程数量：" + threadPoolExecutor.getMaximumPoolSize());
                System.out.println("当前线程数量：" + threadPoolExecutor.getPoolSize());
                System.out.println("活跃线程数量：" + threadPoolExecutor.getActiveCount());
                System.out.println("任务总数：" + threadPoolExecutor.getTaskCount());
                System.out.println("已完成的任务数量：" + threadPoolExecutor.getCompletedTaskCount());
                System.out.println("队列大小：" + threadPoolExecutor.getQueue().size());
                System.out.println("拒绝策略：" + threadPoolExecutor.getRejectedExecutionHandler());
                System.out.println("曾经创建过的最大线程数量：" + threadPoolExecutor.getLargestPoolSize());

                // 使用线程池处理客户端请求
                executor.execute(new ClientHandler(clientSocket));

                // 获取线程池的各种信息。已测试过，在sleep期间上面会收不到连接！！
                Thread.sleep(10000);
                System.out.println("处理任务后线程池的状态：---------------------------------------------------");
                System.out.println("核心线程数量：" + threadPoolExecutor.getCorePoolSize());
                System.out.println("最大线程数量：" + threadPoolExecutor.getMaximumPoolSize());
                System.out.println("当前线程数量：" + threadPoolExecutor.getPoolSize());
                System.out.println("活跃线程数量：" + threadPoolExecutor.getActiveCount());
                System.out.println("任务总数：" + threadPoolExecutor.getTaskCount());
                System.out.println("已完成的任务数量：" + threadPoolExecutor.getCompletedTaskCount());
                System.out.println("队列大小：" + threadPoolExecutor.getQueue().size());
                System.out.println("拒绝策略：" + threadPoolExecutor.getRejectedExecutionHandler());
                System.out.println("曾经创建过的最大线程数量：" + threadPoolExecutor.getLargestPoolSize());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
        }

    }
}

