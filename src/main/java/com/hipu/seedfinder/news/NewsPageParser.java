package com.hipu.seedfinder.news;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.internal.Lists;
import com.hipu.crawlcommons.config.Config;
import com.hipu.seedfinder.tools.HomePageFinder;

/**
 * @author weijian
 *         Date : 2013-05-07 15:32
 */
public class NewsPageParser {
    public static final Logger LOG = Logger.getLogger(NewsPageParser.class);

    @Parameter(description="Help", names={"--help","-help"})
    private boolean help = false;

    @Parameter(description="input file", names="-f", required = true)
    private String file;

    @Parameter(description="output file", names="-o", required = true)
    private String outFile;
    
    @Parameter(description="iutput page file", names="-p", required = true)
    private String pageFile;

    @Parameter(description="Thread num", names="-t")
    private int threadNum = 20;

    private ExecutorService executorService;
    private SingleNewsParser finder;
    protected HttpClient client;
    
    private Map<String, String> urlContents;

    public NewsPageParser() {
    	
        PoolingClientConnectionManager manager = new PoolingClientConnectionManager();
        manager.setMaxTotal(20);
        manager.setDefaultMaxPerRoute(5);
        
        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
        params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
        params.setParameter(ClientPNames.HANDLE_REDIRECTS, false);

        client = new DefaultHttpClient(manager, params);
        
        executorService = Executors.newFixedThreadPool(threadNum);
        urlContents = new HashMap<String, String>();
        
    }
    
    // read json file contains pageinfo.
    public void loadPage() {
        BufferedReader br;
        String line;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(pageFile), "utf-8"));
			while( (line=br.readLine()) != null){
				if (line.length() < 3)
					continue;
				JSONObject obj = JSONObject.parseObject(line);
				urlContents.put(obj.getString("url"), obj.getString("content"));
			}
			LOG.debug("initial page info finished. total size: "+ urlContents.size());
		} catch (UnsupportedEncodingException e) {
			LOG.error("UnsupportedEncodingException");
		} catch (FileNotFoundException e) {
			LOG.error("FileNotFoundException");
		} catch (IOException e) {
			LOG.error("IOException");
		}
    }
    
    public void initial() {
    	loadPage();
    	this.finder = new SingleNewsParser(urlContents);
    }

    
    public void find() {
    	BufferedReader br = null;
    	List<String> records = Lists.newArrayList();
    	String line;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
			while( (line=br.readLine()) != null){
	        	if (line.length() < 3)
	        		continue;
	            records.add(line);
	        }
            LOG.debug("the size of urls to be extracted is : " + records.size());
		} catch (UnsupportedEncodingException e) {
			LOG.error("UnsupportedEncodingException : " + file);
		} catch (FileNotFoundException e) {
			LOG.error("FileNotFoundException : " + file);
		} catch (IOException e) {
			LOG.error("IOException : " + file);
		}

        _find(records.toArray(new String[0]));
    }


    private void _find(String [] records) {
        if ( records == null ) return;

        int len = records.length;
        if ( len == 0) return;

        List<Future<String>> futures = Lists.newArrayList();
        for ( String record : records ){
            futures.add(executorService.submit(new FetchUrlJob(finder, record)));
        }

        BufferedWriter writer = null;
        if ( outFile != null  ) {
            try {
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "utf-8"));
			} catch (UnsupportedEncodingException e) {
				LOG.error("UnsupportedEncodingException : " + outFile);
			} catch (FileNotFoundException e) {
				LOG.error("FileNotFoundException : " + outFile);
			}
        } else {
            writer = new BufferedWriter(new OutputStreamWriter(System.out));
        }
		try {
			for (Future<String> f : futures) {
				writer.write(f.get());
				writer.write('\n');
			}
			writer.close();
		} catch (IOException e) {
			LOG.error("IOException : " + outFile);
		} catch (InterruptedException e) {
			LOG.error("InterruptedException : " + outFile);
		} catch (ExecutionException e) {
			e.printStackTrace();
			LOG.error("ExecutionException : " + outFile);
		}
    }

    public void shutdown() {
        executorService.shutdown();
    }
    
    private class FetchUrlJob implements Callable<String>{
        private String record;
        private SingleNewsParser finder;
        
        private FetchUrlJob(SingleNewsParser finder, String record) {
        	this.record = record;
        	this.finder = finder;
        }

        @Override
        public String call() throws Exception {
        	String url = record.split("\t")[0];
//        	String source = record.split("\t")[2];
        	
//        	String result = this.finder.extractNewsUrl(url);
//        	LOG.debug("original news url : " + url);
//        	LOG.debug("extract  news url : " + result);
        	
        	String result = this.finder.getNewsPageType(url);
        	return result + "\t" + record;
        }
    }
    
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, ConfigurationException {
        URL url = NewsPageParser.class.getResource("/log4j.properties");
        if ( url == null ) {
            url =  NewsPageParser.class.getResource("/conf/log4j.properties");
        }
        PropertyConfigurator.configure(url);
        
        Config conf = Config.getInstance();
        Configuration config = new PropertiesConfiguration(NewsPageParser.class.getResource("/config.properties"));
        conf.registerConfig(config);

        NewsPageParser newsFinder = new NewsPageParser();
        JCommander commander = new JCommander(newsFinder);
        try {
            commander.parse(args);
        } catch (ParameterException e) {
            LOG.error(e.getMessage());
            commander.usage();
        }
        if ( newsFinder.help ){
            commander.usage();
        }else{
        	newsFinder.initial();
        	newsFinder.find();
        	newsFinder.shutdown();
        }
        
    }

}

