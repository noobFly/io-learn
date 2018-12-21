package com.noob.learn.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.noob.learn.bio.Response;

public class NioServer {
    // Channel[Server Client] Selector Buffer
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(8080));
        System.out.println("服务器启动, 服务地址: " + serverSocketChannel.getLocalAddress());

        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        Response response = new Response();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        Map<SocketChannel, Integer> map = new HashMap();

        while (true) {
            // 整个过程，单线程只有这类是阻塞的
            int select = selector.select();
            if (select == 0) {
                continue;
            }
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                    SocketChannel clientChannel = serverChannel.accept();
                    System.out.println("客户端连接: " + clientChannel.getRemoteAddress());
                    clientChannel.configureBlocking(false);
                    clientChannel.register(selector, SelectionKey.OP_READ);
                }

                if (key.isReadable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    Integer newVal = map.computeIfPresent(channel, (t, oldVal) -> oldVal.intValue() + 1);
                    if (newVal == null) {
                        map.computeIfAbsent(channel, t -> 1);
                    }

                    channel.read(buffer);
                    String request = new String(buffer.array()).trim();
                    buffer.clear();
                    System.out.println("接收到客户端" + channel.getRemoteAddress() + "的信息： " + request);
                    channel.write(ByteBuffer.wrap(response.handle(request, map.get(channel)).getBytes()));
                }
                iterator.remove();
            }

        }
    }
}
