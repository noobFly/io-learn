package com.noob.learn.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

/**
 * Reactor
 * 
 */
public class NioServer {
    private static ServerSocketChannel serverSocketChannel = null; //用于监听客户端的连接，所有客户端连接的父管道
    private static Selector            selector            = null; // 多路复用器

    public static void main(String[] args) throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(8080)); 
        serverSocketChannel.configureBlocking(false);// 绑定监听端口，设置连接为非阻塞模式

        System.out.println("服务器启动, 服务地址: " + serverSocketChannel.getLocalAddress());

        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT); //将ServerSocketChannel注册到Reactor线程的多路复用器Selector上，监听ACCEPT事件
        new IOHandler(selector, true).exectue();
    }

}
