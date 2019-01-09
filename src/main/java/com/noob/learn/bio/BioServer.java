package com.noob.learn.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import lombok.extern.slf4j.Slf4j;

/**
 * 总结： 两次阻塞 1、等待连接 2、 等待数据读取
 * <p>
 * 1. 单线程处理的阻塞是由于单线程导致的，与阻塞IO的真实原因不是一个概念。
 * <p>
 * 2. InputStream中读取数据的方式要与OutputStream中的输出数据格式相匹配才能有效快速的解析出数据（是否需要\n）;
 * <p>
 * 读取方式可以不同，但相同的是解析不到数据时阻塞； 不同读取方式在客户端socket正常关闭OutputStream与非正常断开的呈现不一样，
 * 具体参看ClientRequestHandler中处理reqesut. 推荐使用handleWithoutLineBreak();
 * <p>
 * 3. OutputStream：
 * 在buffer中，如果消息的字节小于buffer容量，是不会立刻推送的，只有输出流中的缓冲区填满时，输出流才真正推送消息。
 * <p>
 * flush()方法可以强迫输出流(或缓冲的流)发送数据，即使此时缓冲区还没有填满 。close() 关闭输出流时存储在输出流的缓冲区中的数据就会丢失
 * 所以关闭(close)输出流时，应先刷新(flush)换冲的输出流：“迫使所有缓冲的输出数据被写出到底层输出流中”
 * <p>
 * 4. shutdownOutput()后再write是无效的.
 * <p>
 */
@Slf4j
public class BioServer {

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(8080);
        log.info("服务器启动, 服务地址: " + serverSocket.getLocalSocketAddress());

        while (true) {
            Socket clientSocket = serverSocket.accept(); //等待客户端连接--阻塞!!!
            // requestHandlerAsync(clientSocket);
            requestHandler(clientSocket); // 可选多线程处理
        }
    }

    /**
     * 单线程执行，客户端独占服务端，当前连接的客户端不释放，其他客户端阻塞.
     */
    private static void requestHandler(Socket clientSocket) throws IOException {
        new RequestHandler(clientSocket).run();
    }

    private static void requestHandlerAsync(Socket clientSocket) throws IOException {
        new Thread(new RequestHandler(clientSocket)).start();
    }
}
