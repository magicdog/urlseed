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
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ImmutableSet;
import com.hipu.crawlcommons.config.Config;
import com.hipu.crawlcommons.dao.SiteInfoDAO;
import com.hipu.crawlcommons.url.URLUtil;

/**
 * @author weijian
 *         Date : 2013-05-07 15:32
 */
public class HomePageFinder {
    public static final Logger LOG = Logger.getLogger(HomePageFinder.class);

    @Parameter(description="Help", names={"--help","-help"})
    private boolean help = false;

    @Parameter(description="input file", names="-f", required = true)
    private String file;

    @Parameter(description="output file", names="-o", required = true)
    private String outFile;

    @Parameter(description="Thread num", names="-t")
    private int threadNum = 20;


    protected HttpClient client;

    private int maxRedir = 3;


    private ExecutorService executorService;
    
    private SiteInfoDAO siteInfoDao;

    public static final ImmutableSet<Integer> REDIR_CODES
            = ImmutableSet.copyOf(new Integer[]{301, 302, 303, 307});

    public HomePageFinder() {

        PoolingClientConnectionManager manager = new PoolingClientConnectionManager();
        manager.setMaxTotal(20);
        manager.setDefaultMaxPerRoute(5);

        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
        params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
        params.setParameter(ClientPNames.HANDLE_REDIRECTS, false);
        params.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);

        client = new DefaultHttpClient(manager, params);
        
        siteInfoDao = SiteInfoDAO.getInstance();
    }


    private class FetchHeadJob implements Callable<String>{
        private HttpClient httpClient;
        private String record;
        
        private FetchHeadJob(HttpClient httpClient, String record) {
            this.httpClient = httpClient;
            this.record = record;
        }

        private String getHeader(String key, HttpResponse resp){
            Header[] headers = resp.getHeaders(key);
            return  (headers == null || headers.length == 0) ? null : headers[0].getValue();
        }

        private Result testURL(String url){
            Result result = null;

            try {
                // Prepare Head method for HTTP request
                HttpHead httpHead = new HttpHead(url);
                HttpResponse resp = httpClient.execute(httpHead);
                int code = resp.getStatusLine().getStatusCode();

                if ( code != 200 ){
                    // redirect
                    if ( REDIR_CODES.contains(code) ){
                        String redirUrl = getHeader("Location", resp);
                        if( redirUrl == null ) {
                            redirUrl = getHeader("location", resp);
                        }

                        redirUrl = new URL(new URL(url), redirUrl).toString();

                        result = new Result(ResultType.REDIR, redirUrl, null);
                    } else {
                        result = new Result(ResultType.ERROR, url, "status:" + code);
                    }
                } else {
                    result = new Result(ResultType.SUCCESS, url, null);
                }

            } catch (Exception e) {
                result = new Result(ResultType.ERROR, url, e.getMessage());
            }
            return result;
        }

        @Override
        public String call() throws Exception {
            String part = record.split("\t")[0];

            String url = part;

            if ( ! url.startsWith("http://") ){
                url = "http://" + part;
            }
            Result result = testURL(url);

            if ( result == null || result.type == ResultType.ERROR ){
                if ( ! url.startsWith("http://") ){
                    url = "http://www." + part;
                    result = testURL(url);
                }
            } else if (result.type == ResultType.REDIR){
                result = testURL(result.url);
            }
            String domain = URLUtil.getDomainName(result.getUrl());
//            if ( siteInfoDao.siteExist(domain) )
//            	return result.toString() + "\t" + domain +"\t" + record + "\ty";
            return result.toString() + "\t" + domain +"\t" + record;
        }
    }


    public void find() throws ExecutionException, InterruptedException, IOException  {
    	executorService = Executors.newFixedThreadPool(threadNum);
    	LOG.info(threadNum);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
        List<String> records = Lists.newArrayList();

        
        String line;
        try {
			while( (line=br.readLine()) != null ){
			    records.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

        _find(records.toArray(new String[0]));
    }


    private void _find(String [] records) throws ExecutionException, InterruptedException, IOException {
        if ( records == null ) return;

        int len = records.length;
        if ( len == 0) return;

        List<Future<String>> futures = Lists.newArrayList();
        for ( String record : records ){
            futures.add(executorService.submit(new FetchHeadJob(client, record)));
        }

        BufferedWriter writer = null;
        if ( outFile != null  ) {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "utf-8"));
        } else {
            writer = new BufferedWriter(new OutputStreamWriter(System.out));
        }
        int i = 0;
        for ( Future<String> f : futures ){
        	i++;
        	LOG.info(i);
            writer.write(f.get());
            writer.write('\n');
        }
        writer.close();
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public static enum ResultType { SUCCESS, REDIR, ERROR }

    public static class Result{
        ResultType type;
        String url;
        String detail;

        public Result(ResultType type, String url, String detail) {
            this.type = type;
            this.url = url;
            this.detail = detail;
        }

        public String getUrl() {
        	return this.url;
        }
        @Override
        public String toString() {
            return String.format("[%s]%s\t%s", type.name(), detail==null?"":detail, url);
        }
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, ConfigurationException {
        URL url = HomePageFinder.class.getResource("/log4j.properties");
        if ( url == null ) {
            url =  HomePageFinder.class.getResource("/conf/log4j.properties");
        }
        PropertyConfigurator.configure(url);
        
        Config conf = Config.getInstance();
        Configuration config = new PropertiesConfiguration(HomePageFinder.class.getResource("/config.properties"));
        conf.registerConfig(config);

        HomePageFinder homePageFinder = new HomePageFinder();
        JCommander commander = new JCommander(homePageFinder);
        try {
            commander.parse(args);
        } catch (ParameterException e) {
            LOG.error(e.getMessage());
            commander.usage();
        }
        if ( homePageFinder.help ){
            commander.usage();
        }else{
            homePageFinder.find();
            homePageFinder.shutdown();
        }
        
    }

}
