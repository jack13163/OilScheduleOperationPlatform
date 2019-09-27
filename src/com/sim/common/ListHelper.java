package com.sim.common;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ListHelper {
	public static void saveListToFile(String filePath, List<Double[]> data) {
		FileWriter fw;
		try {
			fw = new FileWriter(filePath);

			// 为了提高写入的效率，使用了字符流的缓冲区。 创建了一个字符写入流的缓冲区对象，并和指定要被缓冲的流对象相关联。
			BufferedWriter bufw = new BufferedWriter(fw);

			for (Double[] line : data) {
				StringBuilder stringBuilder = new StringBuilder();
				for (int i = 0; i < line.length; i++) {
					stringBuilder.append(line[i]);
					if (i < line.length - 1) {
						stringBuilder.append(",");
					}
				}
				bufw.write(stringBuilder.toString());
				bufw.newLine();
			}

			// 使用缓冲区中的方法，将数据刷新到目的地文件中去。
			bufw.flush();
			// 关闭缓冲区,同时关闭了fw流对象
			bufw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
