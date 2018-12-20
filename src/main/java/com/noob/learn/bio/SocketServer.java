package com.noob.learn.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 单线程阻塞
 *
 */
public class SocketServer {

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("服务器启动, 服务地址: " + serverSocket.getLocalSocketAddress());

        while (true) {
            Socket clientSocket = serverSocket.accept(); //等待客户端连接--阻塞!!!
            new ClientRequestHandler(clientSocket, new Response()).run();
        }
    }
}
