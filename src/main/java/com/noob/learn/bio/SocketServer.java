package com.noob.learn.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class SocketServer {

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("服务器启动, 服务地址: " + serverSocket.getLocalSocketAddress());

        while (true) {
            Socket clientSocket = serverSocket.accept(); //等待客户端连接--阻塞!!!
            System.out.println("接收到客户端连接: " + clientSocket.getRemoteSocketAddress());
            new ClientRequestHandler(clientSocket, new Response()).run();
        }
    }
}
