package com.noob.learn.bio;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * 与客户端通讯处理
 */
public class ClientRequestHandler implements Runnable {

    private Socket       clientSocket; //独占式客户端的连接socket
    private Response     response;    //响应自定义处理

    private InputStream  inputStream; // 输入请求
    private OutputStream outputStream; // 输出响应

    public ClientRequestHandler(Socket clientSocket, Response response) throws IOException {
        this.clientSocket = clientSocket;
        this.response = response;
        init();

    }

    private void init() throws IOException {
        inputStream = clientSocket.getInputStream();
        outputStream = clientSocket.getOutputStream();
    }

    /**
     * 接收请求并返回响应。 接收、响应的字符中是否带\n以读取方式有关
     */
    public void run() {
        try {
            handleWithoutLineBreak();
            // handleWithLineBreak1();
            //  handleWithLineBreak2();
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
        while (br.read(buffer) != -1) { // 每次读取的存储位置都是从0开始。 read(b, 0, b.length)。经过本场景下的测试，开始位置还必须是0 !!!!
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
                turns++;
            }
        }

    }

    private void handleWithLineBreak2() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        int turns = 1;
        while (br.readLine() != null) {
            outwrite(turns, br.readLine());
            turns++;
        }
    }

    private void outwrite(int turns, String request) throws IOException {
        System.out.println("接收到客户端信息： " + request);

        //在本测试用例中，是否需要输出"\n" 由客户端的读取方式而定！
        String msg = response.handle(request, turns);
        outputStream.write(msg.getBytes());
    }
}
