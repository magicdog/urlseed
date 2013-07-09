package com.hipu.seedfinder.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.hipu.crawlcommons.config.Config;
import com.hipu.crawlcommons.dao.SiteInfoDAO;
import com.hipu.crawlcommons.url.URLUtil;
import com.hipu.seedfinder.utils.URLFilter;

public class URLSFilter {
	
	public static final Logger LOG = Logger.getLogger(URLSFilter.class);
	
	private Map<String, String> urls;

	private URLFilter filter;

	@Parameter(description = "Help", names = { "--help", "-help" })
	private boolean help = false;

	@Parameter(description = "input file", names = "-f", required = true)
	private String file;

	@Parameter(description = "output file", names = "-o", required = true)
	private String outFile;

	public URLSFilter() {
		this.urls = new HashMap<String, String>();
		this.filter = URLFilter.getInstance();
	}
	
	public void filter() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), "utf-8"));
		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outFile), "utf-8"));

		String line = null;
		int count = 0;
		int index = 0;
		try {
			while ((line = br.readLine()) != null) {
				index ++;
				LOG.info(index);
				if (line.length() < 3)
					continue;
				line = line.toLowerCase();
//				String source = line.split("\t")[2];
//				String category = line.split("\t")[1];
				String url = line.split("\t")[0];
				
//				String domain = URLUtil.getDomainName(url);
//				if (filter.obeyConditions(url, category, source)) {
//					urls.put(url, line+"\t"+domain);
//				}
//				if (filter.obeyConditions(url, null)) {
//					urls.put(URLUtil.getHost(url), line+"\t"+domain);
					urls.put(URLUtil.getHost(url), line);
//				} else
					count ++;
			}
			LOG.warn("throw :"+count);
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (String key : urls.keySet()) {
			wr.write(urls.get(key)+"\n");
		}
		br.close();
		wr.close();
	}
	 
	 public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, ConfigurationException {
	        URL url = URLSFilter.class.getResource("/log4j.properties");
	        if ( url == null ) {
	            url =  URLSFilter.class.getResource("/conf/log4j.properties");
	        }
	        PropertyConfigurator.configure(url);
	        
	        Config conf = Config.getInstance();
	        Configuration config = new PropertiesConfiguration(HomePageFinder.class.getResource("/config.properties"));
	        conf.registerConfig(config);

	        URLSFilter filter = new URLSFilter();
	        JCommander commander = new JCommander(filter);
	        try {
	            commander.parse(args);
	        } catch (ParameterException e) {
	            LOG.error(e.getMessage());
	            commander.usage();
	        }
	        if ( filter.help ){
	            commander.usage();
	        }else{
	        	filter.filter();
	        }
	        
	    }

}
