package com.noob.learn.nio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

/**
 * 客户端与服务端的信息处理合并在一起。
 * <p>
 * 主要是表述：无论是BIO还是NIO例子中，站在Socket(ServerSocket->ServerSocket.accept())、
 * SocketChannel
 * (ServerSocketChannel->ServerSocketChannel.accept())各自的视角，自己的输出是对方的输入
 * ，自己的输入是对方的输出.
 * 自己的localAddress是对面的RemoteAddress,自己的RemoteAddress是对面的localAddress.
 */
@Slf4j
public class IOHandler {
    private Selector                    selector;                                         //多路复用器
    private boolean                     stop      = false;                                //是否中断执行
    private boolean                     isServer  = false;                                // 是否是服务端

    private Map<SocketChannel, String>  msgMap    = new HashMap<SocketChannel, String>(); //客户端传入的消息集合
    private Map<SocketChannel, Integer> acceptMap = new HashMap<SocketChannel, Integer>(); //客户端传入消息的次数

    private int                         time      = 0;                                    //客户端发送消息的次数

    public IOHandler(Selector selector, boolean isServer) {
        this.selector = selector;
        this.isServer = isServer;
    }

    /**
     * 在while循环体中循环遍历selector，它的休眠时间为1s，
     * 无论是否有读写等事件发生，selector每隔1s都被唤醒一次，selector也提供了一个无参的select方法。
     * 当有处于就绪状态的Channel时，selector将返回就绪状态的Channel的SelectionKey集合，
     * 通过对就绪状态的Channel集合进行迭代，可以进行网络的异步读写操作。
     */
    public void exectue() {
        while (!stop) {
            try {

                int count = selector.select(1000);
                if (count > 0) {
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectedKeys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = (SelectionKey) iterator.next();
                        try {
                            handleMsgFromInput(key);//这里可以用线程池启线程去单独处理客户端的请求业务 
                        } catch (Exception e) {
                            if (key != null) {
                                key.cancel();
                                if (key.channel() != null)
                                    key.channel().close();
                            }
                        }
                        iterator.remove();

                    }
                }

            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        // 多路复用器关闭后，所有注册在上面的Channel和Pipe等资源都会被自动去注册并关闭，所以不需要重复释放资源
        if (selector != null)
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private void handleMsgFromInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            //根据SelectionKey的操作位进行判断即可获知网络事件的类型

            if (key.isConnectable()) { // 主要针对于客户端
                /**
                 * 如果是处于连接状态，说明服务端已经返回ACK应答消息。
                 * 对连接结果进行判断，调用SocketChannel的finishConnect()方法，
                 * 如果返回值为true，说明客户端连接成功；如果返回值为false或者直接抛出IOException，说明连接失败。
                 * 在本例程中，返回值为true，说明连接成功。
                 */
                SocketChannel sc = (SocketChannel) key.channel();// localAddress: null;  remoteAddress: localhost/127.0.0.1:8080
                if (sc.finishConnect()) {
                    //将SocketChannel注册到多路复用器上，注册SelectionKey.OP_READ操作位，监听网络读操作，然后发送请求消息给服务端。
                    sc.register(selector, SelectionKey.OP_WRITE); // localAddress: /127.0.0.1:51083;  remoteAddress: localhost/127.0.0.1:8080
                } else {
                    log.info(String.format("客户端%s连接失败!", sc.getLocalAddress()));
                    System.exit(1);// 连接失败，进程退出
                }
            }

            if (key.isAcceptable()) { // 主要针对于服务端
                /**
                 * 通过ServerSocketChannel的accept接收客户端的连接请求并创建SocketChannel实例，
                 * 完成上述操作后，相当于完成了TCP的三次握手，TCP物理链路正式建立。
                 * 注意，需要将新创建的SocketChannel设置为异步非阻塞，同时也可以对其TCP参数进行设置。
                 */
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                SocketChannel sc = ssc.accept();//If this channel is in non-blocking mode then this method will immediately return null if there are no pending connections. Otherwise it will block indefinitely until a new connection is available or an I/O error occurs. 

                sc.configureBlocking(false);// localAddress: /0.0.0.0:8080 ;  remoteAddress: /127.0.0.1:51066
                sc.register(selector, SelectionKey.OP_READ);
            }

            if (key.isReadable()) {
                read(key);
            }

            if (key.isWritable()) {
                write(key);
            }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 首先创建一个ByteBuffer，由于事先无法得知客户端发送的码流大小，
     * 作为例子，开辟一个1M的缓冲区。然后调用SocketChannel的read方法读取请求码流。
     * <p>
     * 注意，由于已经将SocketChannel设置为异步非阻塞模式，因此它的read是非阻塞的。 使用返回值进行判断，看读取到的字节数
     */
    private void read(SelectionKey key) throws IOException, UnsupportedEncodingException, ClosedChannelException {
        SocketChannel sc = (SocketChannel) key.channel();

        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        int readBytes = sc.read(readBuffer);
        /**
         * 返回值有以下三种可能的结果
         * <p>
         * 返回值大于0：读到了字节，对字节进行编解码；
         * <p>
         * 返回值等于0：没有读取到字节，属于正常场景，忽略；
         * <p>
         * 返回值为-1：链路已经关闭，需要关闭SocketChannel，释放资源。
         */
        if (readBytes > 0) {
            /**
             * 当读取到码流以后进行解码，首先对readBuffer进行flip操作，
             * 它的作用是将缓冲区当前的limit设置为position，position设置为0，用于后续对缓冲区的读取操作。
             * 然后根据缓冲区可读的字节个数创建字节数组 ，调用ByteBuffer的get操作将缓冲区可读的字节数组复制到新创建的字节数组中.
             */
            readBuffer.flip();
            byte[] bytes = new byte[readBuffer.remaining()];
            readBuffer.get(bytes);
            String readInMsg = new String(bytes, "UTF-8");

            if (isServer) {
                msgMap.put(sc, readInMsg);
                acceptMap.compute(sc, (mapKey, oldVal) -> {
                    return oldVal == null ? new Integer(0) : oldVal + 1;
                });
            }

            log.info(String.format("接收到客户端%s的信息： %s", sc.getRemoteAddress(), readInMsg));

            sc.register(selector, SelectionKey.OP_WRITE);

        } else if (readBytes < 0) {
            // 对端链路关闭
            key.cancel();
            sc.close();
            stop = true;
        } else {
            // 读到0字节，忽略
        }

    }

    /**
     * 首先将字符串编码成字节数组，根据字节数组的容量创建ByteBuffer，
     * 调用ByteBuffer的put操作将字节数组复制到缓冲区中，然后对缓冲区进行flip操作，
     * 最后调用SocketChannel的write方法将缓冲区中的字节数组发送出去。
     * <p>
     * 由于SocketChannel是异步非阻塞的，它并不保证一次能够把需要发送的字节数组发送完，
     * 此时会出现“写半包”问题，所以需要注册写操作，不断轮询Selector将没有发送完的ByteBuffer发送完毕，
     * 可以通过ByteBuffer的hasRemain()方法判断消息是否发送完成。 此处仅仅是个简单的入门级例程，没有演示如何处理“写半包”场景。
     */

    private void write(SelectionKey key) throws IOException {
        String sendMsg = null;
        SocketChannel sc = (SocketChannel) key.channel();

        if (isServer) {
            String acceptMsg = msgMap.get(sc);
            Integer time = acceptMap.get(sc);
            sendMsg = String.format("服务端的感谢%s, 因为:%s ", time, acceptMsg);
        } else {
            time++;
            sendMsg = String.format("客户端的慰问%s", time);
        }

        byte[] bytes = sendMsg.getBytes();
        ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
        writeBuffer.put(bytes);
        writeBuffer.flip();
        sc.write(writeBuffer);
        log.info(String.format("发出信息->>>>%s", sendMsg));

        if (writeBuffer.hasRemaining()) {
            sc.register(selector, SelectionKey.OP_WRITE); // writeBuffer 可以是一个全局共享内存达到持续写入的效果
        } else {
            sc.register(selector, SelectionKey.OP_READ);
        }

    }

}
