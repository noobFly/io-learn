package com.noob.learn.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import lombok.extern.slf4j.Slf4j;

/**https://mp.weixin.qq.com/s/f3Xe4scj2zJNY3R5A--wTw
 * Reactor
 */
@Slf4j
public class NioServer {
    private static ServerSocketChannel serverSocketChannel = null; //用于监听客户端的连接，所有客户端连接的父管道
    private static Selector            selector            = null; // 多路复用器
	/**
	 * 
	 * 同一个端口只能被一个ServerSocketChannel绑定监听。 否则报错：
	 * <p>
       Exception in thread "main" java.net.BindException: Address already in use: bind

	 */
    public static void main(String[] args) throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(8080));
        serverSocketChannel.configureBlocking(false);// 如果一个 Channel 要注册到 Selector 中，那么这个 Channel 必须是非阻塞的

        log.info("服务器启动, 服务地址: " + serverSocketChannel.getLocalAddress());

        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT); //将ServerSocketChannel注册到Reactor线程的多路复用器Selector上，监听ACCEPT事件
        // 一个 Channel 仅仅可以被注册到一个 Selector 一次，如果将 Channel 注册到 Selector 多次，那么其实就是相当于更新 SelectionKey 的 interest set.
        // channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        new IOHandler(selector, true).exectue();
    }

}
