package com.noob.learn.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 两次阻塞 1、等待连接 2、 等待数据读取
 * <p>
 * 若非多线程，那就是客户端独占服务端，当前连接的客户端不释放，其他客户端阻塞!!!
 */
public class SocketServerASync {

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("服务器启动, 服务地址: " + serverSocket.getLocalSocketAddress());

        while (true) {
            Socket clientSocket = serverSocket.accept(); //等待客户端连接--阻塞!!!
            System.out.println("接收到客户端连接: " + clientSocket.getRemoteSocketAddress());
            new Thread(new ClientRequestHandler(clientSocket, new Response())).start(); // 多线程处理。 若非多线程，那就是客户端独占服务端，当前连接的客户端不释放，其他客户端阻塞!!!
        }
    }
}
