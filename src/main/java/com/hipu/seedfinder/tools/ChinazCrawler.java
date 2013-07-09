package com.hipu.seedfinder.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.internal.Lists;
import com.hipu.crawlcommons.config.Config;
import com.hipu.seedfinder.chinaz.FetchEntime;
import com.hipu.seedfinder.chinaz.FetchThread;
import com.hipu.seedfinder.chinaz.WriteCrawledThread;
import com.hipu.seedfinder.chinaz.WriteFileThread;
import com.hipu.seedfinder.news.NewsUrlFinder;

public class ChinazCrawler {
	public static final Logger LOG = Logger.getLogger(ChinazCrawler.class);

    @Parameter(description="Help", names={"--help","-help"})
    private boolean help = false;

    @Parameter(description="input file", names="-f", required = true)
    private String inputFile;

    @Parameter(description="output file", names="-o", required = true)
    private String outFile;
    
    @Parameter(description="cawled url file", names="-c" )
    private String crawledFile;
    
    @Parameter(description="history cawled url file", names="-h" )
    private String hisCrawledFile;
    
    @Parameter(description="cawled url set", names="-u")
    private String urlsFile;
    
    @Parameter(description="Page num", names="-p",required = true)
    private static int pageNum = 20;

    @Parameter(description="Thread num", names="-t")
    private int threadNum = 20;

    private ExecutorService executorService;
    
    private Timer timer;
    
    private WriteFileThread writer;
    
    private WriteCrawledThread crawledWriter;
    
    public static ConcurrentSkipListSet<String> crawledUrls = new ConcurrentSkipListSet<String>();
    
    public static  BlockingQueue<String> crawlUrls = new LinkedBlockingDeque<String>();
    
    public static ConcurrentSkipListSet<String> totalUrls = new ConcurrentSkipListSet<String>();

    public static  BlockingQueue<String> crawledRecords = new LinkedBlockingDeque<String>();
    
    public static  BlockingQueue<String> records = new LinkedBlockingDeque<String>();
    
    public static String entime = null;
    
    public static String encode = null;
    
    private static int urlsSize;
    
    public ChinazCrawler() {
    	timer = new Timer();
    }
    
    public void init() {
    	executorService = Executors.newFixedThreadPool(threadNum);
    	writer = new WriteFileThread(outFile);
    	crawledWriter = new WriteCrawledThread(crawledFile);
    	writer.start();
    	crawledWriter.start();
    	timer.schedule(new FetchEntime(), 0,30000);
        try {
			Thread.sleep(3000);
		} catch (InterruptedException e1) {
			LOG.error("",e1);
		}
        if (entime == null || encode == null) {
        	LOG.error("can not get etime, please check the network!");
        	System.exit(0);
        }
        loadUrl();
    }
    
    public void loadUrl() {
		BufferedReader br;
		String line;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					inputFile), "utf-8"));
			while ((line = br.readLine()) != null) {
				if (line.length() < 3)
					continue;
				crawlUrls.put(line);
			}
			urlsSize = crawlUrls.size();
			LOG.info("initial page info finished. total size: "
					+ crawlUrls.size());
			if (hisCrawledFile != null) {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(hisCrawledFile),"utf-8"));
				 while ((line=br.readLine()) !=null) {
					 if (line.length() < 3)
							continue;
					crawledUrls.add(line.split("\t")[0]);
				 }
				 LOG.info("initial history info finished. total size: "
							+ crawledUrls.size());
			}
			if (urlsFile != null){
				 br = new BufferedReader(new InputStreamReader(new FileInputStream(urlsFile),"utf-8"));
				 while ((line=br.readLine()) !=null) {
					 if (line.length() < 3)
							continue;
					totalUrls.add(line.split("\t")[0]);
				 }
				 LOG.info("initial total url info finished. total size: "
							+ totalUrls.size());
			}
		} catch (UnsupportedEncodingException e) {
			LOG.error("UnsupportedEncodingException",e);
		} catch (FileNotFoundException e) {
			LOG.error("FileNotFoundException",e);
		} catch (IOException e) {
			LOG.error("IOException",e);
		} catch (InterruptedException e) {
			LOG.error("",e);
		}
    }
    
    public void find() {
        if ( crawlUrls == null ) return;
        int len = crawlUrls.size();
        if ( len == 0) return;
        
		String record = null;
		while (notFinished()) {
			record = crawlUrls.poll();
			if (record == null )
				continue;
			if (!crawledUrls.contains(record))
				executorService.execute(new FetchThread(record));
		}
    }
    
    public static boolean notFinished() {
    	return totalUrls.size() < pageNum;
    }

    public void shutdown() {
        executorService.shutdown();
        timer.cancel();
        writer.stop();
        crawledWriter.stop();
        try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    public static void crawledInfo() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("\nthe amount of urls  to  be   crawled : ").append(crawlUrls.size()).append("\n");
    	sb.append("the amount of urls have been crawled : ").append(crawledUrls.size()).append("\n");
    	for (String str : crawledUrls) {
//    		sb.append(str).append("\n");
    	}
    	sb.append("total amount of urls have    crawled : ").append(totalUrls.size()).append("\n");
    	for (String str : totalUrls) {
//    		sb.append(str).append("\n");
    	}
    	sb.append("the amount of urls in this patch     : ").append(urlsSize).append("\n");
    	LOG.info(sb.toString());
    }
    
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, ConfigurationException {
        URL url = ChinazCrawler.class.getResource("/log4j.properties");
        if ( url == null ) {
            url =  ChinazCrawler.class.getResource("/conf/log4j.properties");
        }
        PropertyConfigurator.configure(url);
        
        Config conf = Config.getInstance();
        Configuration config = new PropertiesConfiguration(ChinazCrawler.class.getResource("/config.properties"));
        conf.registerConfig(config);

        ChinazCrawler chinaCrawl = new ChinazCrawler();
        JCommander commander = new JCommander(chinaCrawl);
        try {
            commander.parse(args);
        } catch (ParameterException e) {
            LOG.error(e.getMessage());
            commander.usage();
        }
        if ( chinaCrawl.help ){
            commander.usage();
        }else{
        	chinaCrawl.init();
        	chinaCrawl.find();
        	chinaCrawl.shutdown();
        }
        
    }
    

}
