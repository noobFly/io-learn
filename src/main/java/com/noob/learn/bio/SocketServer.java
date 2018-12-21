package com.noob.learn.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 单线程阻塞 ,客户端独占服务端，当前连接的客户端不释放，其他客户端阻塞!!!
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
