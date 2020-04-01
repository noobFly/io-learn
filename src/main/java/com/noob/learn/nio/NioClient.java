package com.noob.learn.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
/**
 * 服务器若下线,
 * <p> 
 *客户端 在判定SocketChannel.finishConnect()时报异常：
 * <p>
 * java.net.ConnectException: Connection refused: no further information
 *
 */
public class NioClient {
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);//创建SocketChannel之后，需要将其设置为异步非阻塞模式
        Selector selector = Selector.open();
        doConnect(socketChannel, selector);
        IOHandler ioHandler = new IOHandler(selector, false);
        ioHandler.exectue();

    }

    /**
     * 首先对SocketChannel的connect()操作进行判断，如果连接成功，
     * 则将SocketChannel注册到多路复用器Selector上，注册SelectionKey.OP_READ;
     * <p>
     * 如果没有直接连接成功，则说明服务端没有返回TCP握手应答消息，
     * 但这并不代表连接失败，需要将SocketChannel注册到多路复用器Selector上SelectionKey.OP_CONNECT，
     * 当服务端返回TCP syn-ack消息后，Selector就能够轮询到这个SocketChannel处于连接就绪状态。
     * 
     * @throws IOException
     */
    private static void doConnect(SocketChannel socketChannel, Selector selector) throws IOException {
        // 如果直接连接成功，则注册到多路复用器上，发送请求消息，读应答
        if (socketChannel.connect(new InetSocketAddress("localhost", 8080))) {
            socketChannel.register(selector, SelectionKey.OP_WRITE);
        } else {
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        }
    }

}
