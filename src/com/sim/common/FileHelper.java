package com.sim.common;

import java.io.IOException;
import java.io.RandomAccessFile;

public class FileHelper {
//	/**
//	 * 删除某一个目录下的所有文件及当前文件夹
//	 * 
//	 * @param dir
//	 */
//	public static void emptyDir(File dir) {
//		File[] files = dir.listFiles();
//		for (File file : files) {
//			if (file.isDirectory()) {
//				emptyDir(file);
//			} else {
//				file.delete();
//			}
//		}
//		dir.delete();
//	}

	/**
	 * 线程安全的文件追加方法
	 * 
	 * @param fileName
	 * @param content
	 */
	public static synchronized void appendContentToFile(String fileName, String content) {
		try {
			// 打开一个随机访问文件流，按读写方式
			RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
			// 文件长度，字节数
			long fileLength = randomFile.length();
			// 将写文件指针移到文件尾。
			randomFile.seek(fileLength);
			randomFile.writeBytes(content + "\r\n");
			randomFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
