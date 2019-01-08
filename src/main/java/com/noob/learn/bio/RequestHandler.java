package com.noob.learn.bio;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Scanner;

/**
 * 与客户端通讯处理
 */
public class RequestHandler implements Runnable {

    private Socket        clientSocket;       //客户端socket

    private InputStream   inputStream;        // 输入请求
    private OutputStream  outputStream;       // 输出响应
    private SocketAddress clientSocketAddress;

    public RequestHandler(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        init();

    }

    private void init() throws IOException {
        inputStream = clientSocket.getInputStream();
        outputStream = clientSocket.getOutputStream();
        clientSocketAddress = clientSocket.getRemoteSocketAddress();

    }

    /**
     * 接收请求并返回响应。 接收、响应的字符中是否带\n以读取方式有关
     */
    public void run() {
        System.out.println(String.format("客户端连接: %s", clientSocketAddress));

        try {
            handleWithoutLineBreak();
            // handleWithLineBreak1();
            // handleWithLineBreak2();
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * 读入时，不要求客户端传入换行。
     */
    private void handleWithoutLineBreak() throws IOException {
        BufferedInputStream br = new BufferedInputStream(inputStream);
        byte[] buffer = new byte[1024];
        int turns = 1;
        while (br.read(buffer) != -1) { // 阻塞直到有数据到来!  每次读取的存储位置都是从0开始。 read(b, 0, b.length)。经过本场景下的测试，开始位置还必须是0 !!!!
            outwrite(turns, new String(buffer));
            turns++;
        }
    }

    /**
     * 读入时，需要客户端传入换行！
     */
    private void handleWithLineBreak1() throws IOException {
        Scanner input = new Scanner(inputStream);
        int turns = 1;
        while (true) {
            if (input.hasNext()) { // 阻塞，等待客户端数据！！
                outwrite(turns, input.nextLine());
                // 如果客户端传递的消息中没有"\n"， 只有在客户端close、shutdownOutput后消息才会全部打印处理; 异常退出时，还抛出异常： java.net.SocketException: Connection reset by peer: socket write error
                turns++;
            }
        }

    }

    private void handleWithLineBreak2() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        int turns = 1;
        while (br.readLine() != null) {
            outwrite(turns, br.readLine());
            // 如果客户端传递的消息中没有"\n" , 当客户端正常shutdownOutput、close 时才会进入当前行执行，得到的结果是null; 异常退出时，服务端抛出异常： java.net.SocketException: Connection reset
            turns++;
        }
    }

    private void outwrite(int turns, String request) throws IOException {
        System.out.println("接收到客户端" + clientSocketAddress + "的信息： " + request);

        //在本测试用例中，是否需要输出"\n" 由客户端的读取方式而定！
        String msg = "服务端的感谢" + turns + ", 因为: " + request + "\n";
        System.out.println("发出信息->>>>" + msg);

        outputStream.write(msg.getBytes());
    }
}
