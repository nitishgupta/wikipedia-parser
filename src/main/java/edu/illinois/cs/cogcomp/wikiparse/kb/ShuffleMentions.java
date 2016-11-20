package edu.illinois.cs.cogcomp.wikiparse.kb;

import edu.illinois.cs.cogcomp.wikiparse.util.Constants;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by nitishgupta on 11/16/16.
 */
public class ShuffleMentions {

	public static Random rand = new Random();

	public static final int max_lines_per_file = 1000000;

	public static int num_of_readers_open;

	public static final String mention_out_file_root = "mentions.";

	public static BufferedReader openBFReader(String filepath) {
		BufferedReader bwr = null;
		try {
			bwr = new BufferedReader(new FileReader(filepath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		assert (bwr!=null);
		return bwr;
	}

	public static List<String> filenamesInFolder(String dir) {
		List<String> filenames = new ArrayList<>();
		for (String filename : new File(dir).list()) {
			if (new File(dir + filename).isFile())
				filenames.add(filename);
		}
		return filenames;
	}

	public static List<BufferedReader> getAllBufferedReaders(String dir) {
		List<BufferedReader> readers = new ArrayList<>();
		List<String> filenames = filenamesInFolder(dir);
		for (String f : filenames) {
			readers.add(openBFReader(dir + f));
		}
		return readers;
	}

	public static void shuffle(String in_dir, String out_dir) throws IOException {
		long startTime = System.currentTimeMillis();
		long rep_startTime = System.currentTimeMillis();
		List<BufferedReader> readers = getAllBufferedReaders(in_dir);
		num_of_readers_open = readers.size();
		System.out.println("Num of readers open : " + num_of_readers_open);
		int file_num = 0;
		int num_total_lines_written = 0;
		int num_lines_written = 0;

		BufferedWriter bwr = new BufferedWriter(new FileWriter(out_dir + mention_out_file_root + file_num));

		while (num_of_readers_open > 0) {
			int reader_num = rand.nextInt(num_of_readers_open);
			String line = readers.get(reader_num).readLine();

			if (line == null) {
				readers.get(reader_num).close();
				readers.remove(reader_num);
				num_of_readers_open--;
			} else {
				bwr.write(line);
				bwr.write("\n");
				num_lines_written ++;
				num_total_lines_written++;
				if (num_lines_written == max_lines_per_file) {
					bwr.close();
					num_lines_written = 0;
					file_num++;
					bwr = new BufferedWriter(new FileWriter(out_dir + mention_out_file_root + file_num));
				}
			}
			if (num_total_lines_written % 100000 == 0) {
				long estimatedTime = System.currentTimeMillis() - rep_startTime;
				rep_startTime = System.currentTimeMillis();
				double tt = ((double) (estimatedTime)) / (1000.0 * 60.0);
				System.out.print(num_total_lines_written + " (" + file_num + ", " + tt + " mins) .. ");
			}
		}
		bwr.close();
		long estimatedTime = System.currentTimeMillis() - startTime;
		double tt = ((double) (estimatedTime)) / (1000.0 * 60.0);
		System.out.print(num_total_lines_written + ". " + tt + " mins) .. ");
	}

	public static void main (String [] args) throws Exception {
		System.out.println("Shuffling ... ");
		shuffle(Constants.wiki_kb_mentionsdir, "/save/ngupta19/wikipedia/wiki_kb/mentions_tf_fbname_shuf/");
	}











}
