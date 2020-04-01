package com.noob.learn;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * 经过测试发现，BIO/NIO的单次的读入多少由定义的buffer大小决定。数据长度没有超出buffer大小将会一次性读入
 * <p>
 * 
 * @author noob
 */
@Slf4j
public class FileAnalysis {

    public static void main(String[] args) throws IOException {
        new FileAnalysis().analysis("2018");
    }

    public String analysis(String mark) throws IOException {
        FileOutputStream fout = null;
        try {
            test("C:\\Users\\noob\\Desktop//report1.txt");
            String method = analysis1("C:\\Users\\noob\\Desktop//method.txt", "methodName");
            File file = new File("C:\\Users\\noob\\Desktop//report.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            fout = new FileOutputStream(file, true);
            StringBuilder sb = new StringBuilder(Strings.nullToEmpty(mark)).append("\n")
                    .append("-------------method---------").append("\n").append(method).append("\n").append("\n");

            fout.write(sb.toString().getBytes());
        } catch (Exception e) {
            log.error("解析：{} 异常!", mark, e);
        } finally {
            if (fout != null)
                fout.close();
        }

        return "success";

    }

    /**
     * @param filePath
     * @throws Exception
     */
    private void test(String filePath) throws Exception {
        BufferedInputStream br = new BufferedInputStream(new FileInputStream(filePath));
        byte[] buffer = new byte[15]; //改变该长度可以控制一次新读入的数据量大小
        StringBuilder sb = new StringBuilder();
        while (br.read(buffer) != -1) {
            String str = new String(buffer);
            if (!Strings.isNullOrEmpty(str))
                sb.append(str);
            //当数据总长度为36时，按复用buffer，且不主动重置buffer的用法，最后输出的值会有重复！ 所以需要自己主动清除！
            Arrays.fill(buffer, (byte) 0);

        }
        System.out.println(sb);
    }

    private String analysis2(String filePath, String mark) throws IOException {
        ByteBuffer directBuffer = ByteBuffer.allocate(100);//改变该长度可以控制一次新读入的数据量大小

        FileChannel fin = null;
        String result = null;
        try {
            fin = new FileInputStream(filePath).getChannel();
            StringBuilder sb = new StringBuilder();
            while (fin.read(directBuffer) != -1) {
                directBuffer.flip();
                byte[] bytes = new byte[directBuffer.remaining()];
                directBuffer.get(bytes);
                String str = new String(bytes, "UTF-8");
                if (!Strings.isNullOrEmpty(str))
                    sb.append(str);
                directBuffer.clear(); // 重置缓冲区的索引位置。防止数据重复 (需结合get、flip的概念一起理解)
            }

            result = analysis(mark, Splitter.on("\n").splitToList(sb).stream().map(t -> JSONObject.parseObject(t))
                    .filter(t -> t != null).collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fin != null)
                fin.close();
            directBuffer = null;
        }

        return result;
    }

    private String analysis1(String filePath, String mark) throws IOException {

        BufferedReader fin = null;
        String result = null;
        try {
            fin = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            List<JSONObject> list = Lists.newArrayList();
            String line ;
            while (( line = fin.readLine() )!= null) {
                list.add(JSONObject.parseObject(line));
            }

            result = analysis(mark, list);

        } catch (Exception e) {
            log.error("解析：{} 异常!", filePath, e);
        } finally {
            if (fin != null)
                fin.close();
        }

        return result;
    }

    private String analysis(String mark, List<JSONObject> list) {
        String key = "costTime";
        int totalCount = list.size();
        List<String> msg = Lists.newArrayList();
        list.stream().collect(Collectors.groupingBy(t -> t.getString(mark))).entrySet().forEach(entry -> {
            String str = "%s, total：%s, avg：%s, max：%s, min：%s, lt_avg：%s, >50:%s, >100:%s, >200:%s, >300:%s, >500:%s";
            List<JSONObject> value = entry.getValue();
            int total = value.size();
            String name = entry.getKey();
            double avg = value.stream().mapToDouble(t -> t.getDouble(key)).average().getAsDouble();
            double max = value.stream().mapToDouble(t -> t.getDouble(key)).max().getAsDouble();
            double min = value.stream().mapToDouble(t -> t.getDouble(key)).min().getAsDouble();
            double lavg = value.stream().filter(t -> t.getDouble(key) > avg).count();

            double p50 = value.stream().filter(t -> t.getDouble(key) > 50).count();

            double p100 = value.stream().filter(t -> t.getDouble(key) > 100).count();
            double p200 = value.stream().filter(t -> t.getDouble(key) > 200).count();
            double p300 = value.stream().filter(t -> t.getDouble(key) > 300).count();
            double p500 = value.stream().filter(t -> t.getDouble(key) > 500).count();

            msg.add(String.format(str, name, total, scale(avg), max, min, lavg, p50, p100, p200, p300, p500));

        });
        return "总条数:" + totalCount + "\n" + Joiner.on("\n").join(msg);
    }

    private String scale(double arg) {
        return new BigDecimal("" + arg).setScale(2, RoundingMode.HALF_UP).toString();
    }
}
