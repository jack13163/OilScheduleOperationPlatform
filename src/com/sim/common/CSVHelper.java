package com.sim.common;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class CSVHelper {

	private String[] headers;
	private char seperator = ',';

	public char getSeperator() {
		return seperator;
	}

	public void setSeperator(char seperator) {
		this.seperator = seperator;
	}

	public String[] getHeaders() {
		return headers;
	}

	public void setHeaders(String[] headers) {
		this.headers = headers;
	}

	public CSVHelper() {
	}

	/**
	 * 读取CVS文件
	 * 
	 * @param path
	 * @param skipHeaders
	 * @return
	 */
	public List<String[]> readCSV(String path, boolean skipHeaders) {

		List<String[]> result = new LinkedList<>();

		try {
			// 第一参数：读取文件的路径; 第二个参数：分隔符; 第三个参数：字符集
			CsvReader csvReader = new CsvReader(path, seperator, Charset.forName("UTF-8"));

			// 是否跳过标题
			if (!skipHeaders) {
				headers = csvReader.getHeaders();
			}

			// 读取每行的内容
			while (csvReader.readRecord()) {

				String[] line = csvReader.getValues();
				result.add(line);
			}

			csvReader.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(0);
		}

		return result;
	}

	/**
	 * 写入csv
	 * 
	 * @param path
	 * @param content
	 */
	public void writeCSV(String path, List<String[]> content) {

		try {
			CsvWriter csvWriter = new CsvWriter(path, seperator, Charset.forName("UTF-8"));

			// 写表头和内容
			csvWriter.writeRecord(headers);
			for (int i = 0; i < content.size(); i++) {
				String[] line = content.get(i);
				csvWriter.writeRecord(line);
			}

			// 关闭csvWriter
			csvWriter.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(0);
		}
	}
}