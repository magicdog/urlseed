package com.hipu.seedfinder.news;

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

public class URLFilter {
	
	public static final Logger LOG = Logger.getLogger(URLFilter.class);
	
	private Map<String, String> urls;
	
	 public final static String[] ExceptKRegExp = {".*[集团 | 厂 | 公司 | 企业 | 店 |制造商 |银行 |论坛|出版社]$"
	    	,".*[联通| 商城|加盟|采购| 招标|投标|交易|批发|下载|快递|租售|订房|酒店|教程|礼品].*"};
	 
	 public final static String[] CategoryInfo = {"商业"};
	
	private SiteInfoDAO siteInfo;
	
	 private List<Pattern> patterns;
	
	 @Parameter(description="Help", names={"--help","-help"})
	    private boolean help = false;

	    @Parameter(description="input file", names="-f", required = true)
	    private String file;

	    @Parameter(description="output file", names="-o", required = true)
	    private String outFile;
	
	public URLFilter() {
		this.urls = new HashMap<String,String>();
		siteInfo = SiteInfoDAO.getInstance();
		patterns = new ArrayList<Pattern>();
		for (String reg : ExceptKRegExp) {
    		this.patterns.add(Pattern.compile(reg));
    	}
	}
	
	 public void filter() throws IOException {
//		 String base = "D:\\workspace\\SeedFinder\\src\\main\\resources\\";
		 BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
		 BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "utf-8"));

	        String line;
	      
	        try {
				while( (line=br.readLine()) != null){
					if (line.length() < 3)
						continue;
					String source = line.split("\t")[0];
					String category = line.split("\t")[1];
					String url = line.split("\t")[2];
					String domain = URLUtil.getDomainName(url); 
					boolean sourceMatch = false;
					
					for (Pattern pattern : this.patterns) {
			    		if (pattern.matcher(source).matches()){
			    			sourceMatch = true;
			    			break;
			    		}
			    	}
					if (sourceMatch)
						continue;
					for (String str: CategoryInfo) {
						if (category.contains(str)) {
							sourceMatch = true;
							break;
						}
					}
					if (!sourceMatch)
						continue;
					if (siteInfo.siteExist(domain))
						continue;
					LOG.info(source+category+url);
					urls.put(url, line);
//	            LOG.info(records.size());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        for (String key : urls.keySet()) {
	        	wr.write(urls.get(key)+"\n");
	        }
	        br.close();
	        wr.close();
	 }
	 
	 public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, ConfigurationException {
	        URL url = URLFilter.class.getResource("/log4j.properties");
	        if ( url == null ) {
	            url =  URLFilter.class.getResource("/conf/log4j.properties");
	        }
	        PropertyConfigurator.configure(url);
	        
	        Config conf = Config.getInstance();
	        Configuration config = new PropertiesConfiguration(URLFilter.class.getResource("/config.properties"));
	        conf.registerConfig(config);

	        URLFilter filter = new URLFilter();
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
