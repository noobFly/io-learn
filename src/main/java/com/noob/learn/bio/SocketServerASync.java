package com.noob.learn.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 两次阻塞 1、等待连接 2、 等待数据读取
 * <p>
 * 若非多线程，那就是客户端独占服务端，当前连接的客户端不释放，其他客户端阻塞!!!
 * <p>
 *  如果已经连接上的客户端非正常断开，则服务端异常<p>
 * Exception in thread "main" java.lang.RuntimeException: java.net.SocketException: Connection reset
    at com.noob.learn.bio.ClientRequestHandler.run(ClientRequestHandler.java:50)
    at com.noob.learn.bio.SocketServer.main(SocketServer.java:19)
Caused by: java.net.SocketException: Connection reset
    at java.net.SocketInputStream.read(SocketInputStream.java:210)
    at java.net.SocketInputStream.read(SocketInputStream.java:141)
    at java.io.BufferedInputStream.fill(BufferedInputStream.java:246)
    at java.io.BufferedInputStream.read1(BufferedInputStream.java:286)
    at java.io.BufferedInputStream.read(BufferedInputStream.java:345)
    at java.io.FilterInputStream.read(FilterInputStream.java:107)
    at com.noob.learn.bio.ClientRequestHandler.handleWithoutLineBreak(ClientRequestHandler.java:62)
    at com.noob.learn.bio.ClientRequestHandler.run(ClientRequestHandler.java:46)
    ... 1 more
 */
public class SocketServerASync {

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("服务器启动, 服务地址: " + serverSocket.getLocalSocketAddress());

        while (true) {
            Socket clientSocket = serverSocket.accept(); //等待客户端连接--阻塞!!!

            new Thread(new ClientRequestHandler(clientSocket, new Response())).start(); // 多线程处理。 若非多线程，那就是客户端独占服务端，当前连接的客户端不释放，其他客户端阻塞!!!
        }
    }
}
