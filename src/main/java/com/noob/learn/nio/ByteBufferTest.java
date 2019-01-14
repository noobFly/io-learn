package com.noob.learn.nio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
/**
 * 
 * https://my.oschina.net/u/3434392/blog/2999202
 *
 */
public class ByteBufferTest {
    public static void main(String[] args) throws IOException {
        ByteBuffer buff = ByteBuffer.allocate(1024);
        System.out.println(String.format("初始化后：hasRemaining: %s", buff.hasRemaining()));
        buff.compact();
        System.out.println(String.format("compact后：hasRemaining: %s", buff.hasRemaining()));

        FileChannel fin = null;
        FileChannel fout = null;
        try {
            fin = new FileInputStream("filein").getChannel();
            fout = new FileOutputStream("fileout").getChannel();
            while (fin.read(buff) != -1) {
                buff.flip();
                fout.write(buff);
                buff.clear();
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
}
