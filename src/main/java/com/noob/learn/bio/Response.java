package com.noob.learn.bio;

/**
 * 自定义处理方法
 */
public class Response {

    public String handle(String request, int time) {
        return "来自服务端的感谢" + time + ", 因为: " + request + "\n"; // 换行符以客户端的接收方式而取舍！
    }

}
