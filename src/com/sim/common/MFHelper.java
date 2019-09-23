package com.sim.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 生成MF文件，用于打包jar
 * 
 * @author Administrator
 */
public class MFHelper {

	public static void main(String[] args) throws IOException {
		getMFFile("com.sim.ui.MainMethod");
		System.out.println("success.");
	}

	public static boolean getMFFile(String mainClassName) throws IOException {

		if (new File(".classpath").exists()) {

			List<String> list = readFile(".classpath");

			Pattern pattern = Pattern.compile("path=\"(.*?)\"");
			Pattern patternCheck = Pattern.compile("kind=\"lib\"");
			Matcher matcher;

			String libStr = "";

			for (String str : list) {

				matcher = patternCheck.matcher(str);
				if (!matcher.find())
					continue;

				matcher = pattern.matcher(str);
				if (matcher.find())
					libStr += matcher.group(1) + " ";
			}

			if (libStr == null || libStr.length() == 0)
				libStr += " ";

			File file = new File("MANIFEST.MF");
			file.delete();

			file.createNewFile();

			writeTxtFile(file, "Manifest-Version: 1.0");
			writeTxtFile(file, "Main-Class: " + mainClassName);
			writeTxtFile(file, "Class-Path: " + libStr);

			return true;
		} else {
			return false;
		}
	}

	private static List<String> readFile(String path) throws IOException {
		List<String> list = new ArrayList<String>();
		FileInputStream fis = new FileInputStream(path);
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
		BufferedReader br = new BufferedReader(isr);
		String line = "";
		while ((line = br.readLine()) != null) {
			if (line.lastIndexOf("---") < 0) {
				list.add(line);
			}
		}
		br.close();
		isr.close();
		fis.close();
		return list;
	}

	private static boolean writeTxtFile(File file, String newStr) throws IOException {
		boolean flag = false;
		String filein = newStr + "\r\n";
		String temp = "";

		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;

		FileOutputStream fos = null;
		PrintWriter pw = null;
		try {
			fis = new FileInputStream(file);
			isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
			StringBuffer buf = new StringBuffer();

			while ((temp = br.readLine()) != null) {
				buf = buf.append(temp);
				buf = buf.append(System.getProperty("line.separator"));
			}
			buf.append(filein);

			fos = new FileOutputStream(file);
			pw = new PrintWriter(fos);
			pw.write(buf.toString().toCharArray());
			pw.flush();
			flag = true;
		} catch (IOException e) {
			throw e;
		} finally {
			if (pw != null) {
				pw.close();
			}
			if (fos != null) {
				fos.close();
			}
			if (br != null) {
				br.close();
			}
			if (isr != null) {
				isr.close();
			}
			if (fis != null) {
				fis.close();
			}
		}
		return flag;
	}

}
