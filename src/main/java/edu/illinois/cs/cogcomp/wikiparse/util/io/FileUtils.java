package edu.illinois.cs.cogcomp.wikiparse.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nitishgupta on 10/20/16.
 */

public class FileUtils {
	private final static Logger logger = LoggerFactory.getLogger(FileUtils.class);

	public static String getTextFromFile(String filename, String charset) {
		try {
			if (filename.endsWith(".gz")) {
				return IOUtils.toString(new GZIPInputStream(new FileInputStream(filename)), charset);
			} else {
				return org.apache.commons.io.FileUtils.readFileToString(new File(filename), charset);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public static List<String> getLines(String filename) {
		List<String> list = null;
		try {
			if (filename.endsWith(".gz")) {
				return IOUtils.readLines(new GZIPInputStream(new FileInputStream(filename)), "UTF-8");
			} else {
				list = org.apache.commons.io.FileUtils.readLines(new File(filename), "UTF-8");
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return list;
	}

	public static String readFileToString(String filename) {
		return getTextFromFile(filename, "UTF8");
	}

	public static String getTextFromFile(String filename) {
		return getTextFromFile(filename, "UTF8");
	}

	public static void writeStringToFile(String filename, String text) {
		try {
			org.apache.commons.io.FileUtils.writeStringToFile(new File(filename), text, "UTF8");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static void writeTextToFile(String text, String filename) {
		try {
			PrintWriter pw =
							new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF8"));
			pw.print(text);
			pw.close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	static public boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	static public void copyFile(String src, String dst) {
		try {
			org.apache.commons.io.FileUtils.copyFile(new File(src), new File(dst));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static List<String> readLines(String filename, String charset) {
		try {
			return org.apache.commons.io.FileUtils.readLines(new File(filename), charset);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public static List<String> readLines(String filename) {
		return readLines(filename, "utf-8");
	}

	public static void writeLines(String filename, Collection<?> data) {
		try {
			org.apache.commons.io.FileUtils.writeLines(new File(filename), data);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
