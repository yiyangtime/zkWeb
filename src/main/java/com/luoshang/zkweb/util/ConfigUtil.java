package com.luoshang.zkweb.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtil {

	private static Properties properties = new Properties();
	static {
		try {
			properties.load(findOtherPathInputStream("zk.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static Properties getProperties() {
		return properties;
	}

	public static void setProperties(Properties properties) {
		ConfigUtil.properties = properties;
	}

	public static String getConfigMessage(String key) {
		if (key != null && key.trim().length() > 0) {
			return properties.getProperty(key);
		}
		return null;
	}

	/**
	 * 读取属性文件内容
	 * 
	 * @param propFile
	 * @return
	 */
	private static InputStream findOtherPathInputStream(String propFile) {
		InputStream inputStream = ConfigUtil.class.getClassLoader().getResourceAsStream(propFile);
		if (inputStream != null)
			return inputStream;
		java.io.File f = null;
		String curDir = System.getProperty("user.dir");
		f = new java.io.File(curDir, propFile);
		if (f.exists())
			try {
				return new java.io.FileInputStream(f);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		String classpath = System.getProperty("java.class.path");
		String[] cps = classpath.split(System.getProperty("path.separator"));

		for (int i = 0; i < cps.length; i++) {
			f = new java.io.File(cps[i], propFile);
			if (f.exists())
				break;
			f = null;
		}
		if (f != null)
			try {
				return new java.io.FileInputStream(f);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		return null;
	}

}
