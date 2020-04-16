package com.noob.learn;

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
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileAnalysis {

	public static void main(String[] args) throws IOException {
		new FileAnalysis().analysis("2018");
	}

	public String analysis(String mark) throws IOException {
		FileOutputStream fout = null;
		try {
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

	

	private String analysis1(String filePath, String mark) throws IOException {

		BufferedReader fin = null;
		String result = null;
		try {
			fin = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
			List<JSONObject> list = Lists.newArrayList();
			String line;
			while ((line = fin.readLine()) != null) {
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
