package com.noob.learn.nio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * https://my.oschina.net/u/3434392/blog/2999202
 */
public class ByteBufferTest {
    public static void main(String[] args) throws IOException {

        System.out.println("before alocate: " + memoryLog());

    /*    // 如果分配的内存过小，调用Runtime.getRuntime().freeMemory()大小不会变化 ! 125761是此次测试的阀值
        ByteBuffer buffer = ByteBuffer.allocate(125761);
        System.out.println("buffer = " + buffer);
        System.out.println("after alocate: " + memoryLog());*/

        // 这部分直接用的系统内存，所以对JVM的内存没有影响   ---只有单独测试才能完全符合设想
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(1736076288);
        System.out.println("directBuffer = " + directBuffer);
        System.out.println("after direct alocate: " + memoryLog());
        System.out.println(String.format("初始化后：hasRemaining: %s", directBuffer.hasRemaining()));
        directBuffer.compact();
        System.out.println(String.format("compact后：hasRemaining: %s", directBuffer.hasRemaining()));
       /* System.out.println("----------Test wrap--------");
        byte[] bytes = new byte[32];
        buffer = ByteBuffer.wrap(bytes);
        System.out.println(buffer);

        buffer = ByteBuffer.wrap(bytes, 10, 10);
        System.out.println(buffer);*/
        

        FileChannel fin = null;
        FileChannel fout = null;
        try {
            fin = new FileInputStream("filein").getChannel();
            fout = new FileOutputStream("fileout").getChannel();
            while (fin.read(directBuffer) != -1) {
                directBuffer.flip();
                byte[] bytes = new byte[directBuffer.remaining()];
                directBuffer.get(bytes);
                System.out.println(new String(bytes, "UTF-8"));
                fout.write(directBuffer);
                directBuffer.clear();
            }
            
           
        } catch (FileNotFoundException e) {

        } finally {
            try {
                if (fin != null) {
                    fin.close();
                }
                if (fout != null) {
                    fout.close();
                }
            } catch (IOException e) {
                throw e;
            }
        }
    }
    
    private static String memoryLog() {
        return String.format("total:%s, max:%s, free:%s", Runtime.getRuntime().totalMemory(), Runtime.getRuntime()
                .maxMemory(), Runtime.getRuntime().freeMemory());
    }

}
