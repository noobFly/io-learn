package com.noob.learn.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 阻塞IO
 */
public class SocketClient {
    static ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) throws IOException, InterruptedException {
        //创建客户端Socket，指定连接服务器地址和端口
        Socket socket = new Socket("localhost", 8080);
        System.out.println("客户端启动:" + socket.getLocalSocketAddress());//每一个客户端分配一个端口号

        OutputStream outputStream = socket.getOutputStream();//字节输出
        PrintWriter outputPrint = new PrintWriter(outputStream);//将输出流包装成打印流

        //获取输出流，定时向服务器端发信息
        Thread outputThread = new Thread(() -> {
            AtomicInteger time = new AtomicInteger(1);
            service.scheduleWithFixedDelay(() -> {
                String msg = "来自客户端的慰问" + time.intValue();
                outputPrint.write(msg);
                System.out.println(msg);
                // outputPrint.println(); // 若服务端使用readLine, 一定要输出一个换行！！ 否则服务端读取不了客户端的输出流。 【经测试，只有在客户端被下线后，才会全部打印出来】
                    outputPrint.flush();
                    time.addAndGet(1);
                }, 0, 2, TimeUnit.SECONDS);

        });

        //获取输入流，并读取服务器端的响应
        InputStream inputStream = socket.getInputStream();
        Scanner inputScanner = new Scanner(inputStream);
        Thread inputThread = new Thread(() -> {
            while (true) {
                if (inputScanner.hasNext()) { //阻塞获取数据！！！
                    System.out.println(inputScanner.nextLine()); // 要求服务端响应一定要输出一个换行！！ 否则读取不了这个响应. 【经测试，只有在服务端被下线后，才会全部打印出来】
                }
            }

        });

        outputThread.start();
        inputThread.start();
        Thread.sleep(100000);
        outputPrint.close();
        outputStream.close();
        socket.close();
    }
}
