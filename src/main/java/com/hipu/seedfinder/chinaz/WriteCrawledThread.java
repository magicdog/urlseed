package com.hipu.seedfinder.chinaz;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.hipu.seedfinder.tools.ChinazCrawler;
import com.hipu.seedfinder.tools.ChinazCrawlerPatch;

public class WriteCrawledThread implements Runnable{

	private String outFile;
	
	private BufferedWriter writer;
	
	private Thread thread;
	
	private boolean flag;
	
	public WriteCrawledThread(String outFile) {
		this.outFile = outFile;
		this.flag = true;
		if (outFile != null) {
			try {
				writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(outFile), "utf-8"));
			} catch (UnsupportedEncodingException e) {
			} catch (FileNotFoundException e) {
			}
		} else {
			writer = new BufferedWriter(new OutputStreamWriter(System.out));
		}
		this.thread = new Thread(this);
	}
	@Override
	public void run() {
		String line = null;
		try {
			while ( flag ) {
				if ( (line=ChinazCrawlerPatch.crawledRecords.poll()) ==null) 
					continue;
				writer.write(line);
				writer.write("\n");
			}
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void start() {
		this.thread.start();
	}
	public void stop() {
		flag = false;
	}

}
