package com.example.socketdemo.service;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    private BufferedInputStream inputStream;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//            inputStream = new BufferedInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String inputLine;
            int count = 0;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("行数：" + (count++) + "客户端消息：" + inputLine);
                out.println("服务端收到消息：" + inputLine);
                if ("end".equals(inputLine)) {
                    break;
                }
            }
//            byte[] bytes = new byte[1024];
//            int count = 0;
//            while ((count = inputStream.read(bytes)) != 0) {
//                System.out.println("读取字节数：" + count);
//                System.out.println("客户端消息：" + new String(bytes));
//                out.println("服务端收到消息：" + new String(bytes));
//                if ("end".equals(bytes)) {
//                    break;
//                }
//            }
            System.out.println("---执行完成---");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.println("关闭当前连接的Socket！");
                out.close();
                in.close();
//                inputStream.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
