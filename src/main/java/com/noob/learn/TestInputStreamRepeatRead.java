package com.noob.learn;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Strings;

/**
 * Java中的Inputstream是不能重复读取的
 *
 */
public class TestInputStreamRepeatRead {
	public static void main(String[] args) {
		try {
			cacheInputStream();
			testReadRate("C:\\Users\\admin\\Desktop\\text.txt");
			testByteBuffer("C:\\Users\\admin\\Desktop\\text.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 可复用InputStream
	 * <p>
	 * 通过ByteArrayOutputStream，以缓存的方式去处理
	 * <p>
	 * pos标识已读的位置
	 * <p>
	 * 通过mark(设置字节流的标记) 和reset(将“字节流中下一个被读取的位置”重置到mark所标记的位置) 方法，以标记和重置的方式实现
	 * BufferedInputStream也可以，但是FileInputStream不支持
	 * <p>
	 * 这个实现与ByteBuffer类似通过标记来控制读写位置
	 */
	private static void cacheInputStream() throws IOException {
		String msg = "大佬你好";
		ByteArrayInputStream inputStream = new ByteArrayInputStream(msg.getBytes()); // 初始时pos|mark都为0
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		IOUtils.copy(inputStream, outputStream);// 输出后： pos为12、mark为0
		// inputStream.mark(12);// mark方法是将 mark值设置为pos值。入参无意义。当前测试场景下可不使用
		System.out.println("第一次：" + outputStream.toString());
		System.out.println("第二次：" + outputStream.toString());// 自带缓存可多次读取

		ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
		IOUtils.copy(inputStream, outputStream2);
		System.out.println("第三次：" + outputStream2.toString()); // 虽然inputStream里的buf还有数据，但是没办法通过OutputStream读出

		ByteArrayOutputStream outputStream3 = new ByteArrayOutputStream();
		inputStream.reset(); // 字节流中下一个被读取的位置重置到mark所标记的位置 mark默认是0
		IOUtils.copy(inputStream, outputStream3);
		System.out.println("第四次：" + outputStream3.toString());
	}

	/**
	 * 
	 * 经过测试发现，BIO/NIO的单次的读入多少由定义的buffer大小决定。数据长度没有超出buffer大小将会一次性读入
	 * <p>
	 * 需要注意的是:
	 * <p>
	 * 按如下方式read到内存中是按字节分批次来，并按每批次来转成String，可能出现乱码（eg. 刚好将中文的字节分成了2批）
	 */
	private static void testReadRate(String filePath) throws Exception {
		BufferedInputStream br = new BufferedInputStream(new FileInputStream(filePath));
		byte[] buffer = new byte[10]; // 改变该长度可以控制一次新读入的数据量大小
		StringBuilder sb = new StringBuilder();
		while (br.read(buffer) != -1) {
			String str = new String(buffer);
			if (!Strings.isNullOrEmpty(str))
				sb.append(str);
			// 当数据总长度为36时，按复用buffer，且不主动重置buffer的用法，最后输出的值会有重复！ 所以需要自己主动清除！
			Arrays.fill(buffer, (byte) 0);

		}
		System.out.println(sb);
	}

	private static void testByteBuffer(String filePath) throws IOException {
		ByteBuffer directBuffer = ByteBuffer.allocate(10);// 改变该长度可以控制一次新读入的数据量大小
		StringBuilder sb = new StringBuilder();

		try (FileChannel fin = new FileInputStream(filePath).getChannel()) {
			while (fin.read(directBuffer) != -1) {
				directBuffer.flip();
				byte[] bytes = new byte[directBuffer.remaining()];
				directBuffer.get(bytes);
				String str = new String(bytes, "UTF-8");
				if (!Strings.isNullOrEmpty(str))
					sb.append(str);
				directBuffer.clear(); // 重置缓冲区的索引位置。防止数据重复 (需结合get、flip的概念一起理解)
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println(sb.toString());
	}

}
