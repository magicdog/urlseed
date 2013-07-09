package com.hipu.seedfinder.news;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
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
import com.google.common.collect.ImmutableSet;
import com.hipu.crawlcommons.config.Config;
import com.hipu.crawlcommons.dao.SiteInfoDAO;
import com.hipu.crawlcommons.url.URLUtil;
import com.hipu.seedfinder.tools.HomePageFinder;
import com.hipu.seedfinder.tools.HomePageFinder.Result;
import com.hipu.seedfinder.tools.HomePageFinder.ResultType;


/**
 * @author weijian
 *         Date : 2013-05-07 15:32
 */
public class NewsUrlFinder {
    public static final Logger LOG = Logger.getLogger(HomePageFinder.class);

    @Parameter(description="Help", names={"--help","-help"})
    private boolean help = false;

    @Parameter(description="input file", names="-f", required = true)
    private String file;

    @Parameter(description="output file", names="-o", required = true)
    private String outFile;

    @Parameter(description="Thread num", names="-t")
    private int threadNum = 20;

    private ExecutorService executorService;
    private SingleUrlFinder finder;
    protected HttpClient client;

    public NewsUrlFinder() {
    	
        PoolingClientConnectionManager manager = new PoolingClientConnectionManager();
        manager.setMaxTotal(20);
        manager.setDefaultMaxPerRoute(5);
        
        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
        params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
        params.setParameter(ClientPNames.HANDLE_REDIRECTS, false);

        client = new DefaultHttpClient(manager, params);
        
        executorService = Executors.newFixedThreadPool(threadNum);
        this.finder = new SingleUrlFinder(client);
    }


    private class FetchUrlJob implements Callable<String>{
        private String record;
        private SingleUrlFinder finder;
        
        private FetchUrlJob(SingleUrlFinder finder, String record) {
        	this.record = record;
        	this.finder = finder;
        }

        @Override
        public String call() throws Exception {
        	String url = record.split("\t")[0];
        	String result = this.finder.getNewsUrl(url);
        	LOG.debug(result);
        	return result + "\t" + record;
        	
//        	boolean isOK = this.finder.urlFetch(url);
//        	if (isOK)
//        		return "[SUCCESS]\t" + record;
//        	return "[FAILED]\t" + record;
        }
    }


    public void find() throws IOException, ExecutionException, InterruptedException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
        List<String> records = Lists.newArrayList();

        String line;
      
        while( (line=br.readLine()) != null){
        	if (line.length() < 3)
        		continue;
            records.add(line);
        }

        _find(records.toArray(new String[0]));
    }


    private void _find(String [] records) throws ExecutionException, InterruptedException, IOException {
        if ( records == null ) return;

        int len = records.length;
        if ( len == 0) return;

        List<Future<String>> futures = Lists.newArrayList();
        for ( String record : records ){
            futures.add(executorService.submit(new FetchUrlJob(finder, record)));
        }

        BufferedWriter writer = null;
        if ( outFile != null  ) {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "utf-8"));
        } else {
            writer = new BufferedWriter(new OutputStreamWriter(System.out));
        }
        for ( Future<String> f : futures ){
            writer.write(f.get());
            writer.write('\n');
        }
        writer.close();
    }

    public void shutdown() {
        executorService.shutdown();
    }
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, ConfigurationException {
        URL url = NewsUrlFinder.class.getResource("/log4j.properties");
        if ( url == null ) {
            url =  NewsUrlFinder.class.getResource("/conf/log4j.properties");
        }
        PropertyConfigurator.configure(url);
        
        Config conf = Config.getInstance();
        Configuration config = new PropertiesConfiguration(HomePageFinder.class.getResource("/config.properties"));
        conf.registerConfig(config);

        NewsUrlFinder newsFinder = new NewsUrlFinder();
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
        	newsFinder.find();
        	newsFinder.shutdown();
        }
        
    }

}

