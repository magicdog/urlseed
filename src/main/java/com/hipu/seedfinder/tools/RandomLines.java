package com.hipu.seedfinder.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import com.beust.jcommander.internal.Lists;

public class RandomLines {
	
	public void randomOut(int size) {
		String inputFile = "D:\\workspace\\SeedFinder\\src\\main\\resources\\filtered21";
		String outputFile = "D:\\workspace\\SeedFinder\\src\\main\\resources\\filtered22";
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			List<String> lines = Lists.newArrayList();
			
			String line;
			while ( (line=reader.readLine()) != null) {
				lines.add(line);
			}
			int i=0;
			int length = lines.size();
			Random random = new Random(System.currentTimeMillis());
			
			while (i<size && i<lines.size()) {
				writer.write(lines.get(random.nextInt(length))+"\n");
				i++;
			}
			
			reader.close();
			writer.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void main(String [] args) {
		RandomLines rl = new RandomLines();
		rl.randomOut(500);
	}
}
