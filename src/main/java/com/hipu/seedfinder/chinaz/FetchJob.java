package com.hipu.seedfinder.chinaz;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.hipu.crawlcommons.url.URLUtil;
import com.hipu.parser.dom.DomUtils;
import com.hipu.seedfinder.tools.ChinazCrawlerPatch;

public class FetchJob implements Callable<String>{
	
	private LocalHttpClient client;
	
	public static final Logger LOG = Logger.getLogger(FetchJob.class);
	
	private String url;
	
	public FetchJob(String url) {
		client = LocalHttpClient.getInstance();
		this.url = url;
	}

	@Override
	public String call() throws Exception {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		try {
			HttpResponse resp = client.fetchUrl(url, ChinazCrawlerPatch.entime);
			Document doc = DomUtils.getDocument(EntityUtils.toByteArray(resp.getEntity()), "utf-8");
			List<String> anchors = DomParser.extractUrl(url, doc);
			if (anchors.size() == 0){
				LOG.warn("can not get the anchors in page : "+url);
				return sb.toString();
			}
			
			for (String anchor : anchors) {
				String url = anchor.split("\t")[0];
				if (ChinazCrawlerPatch.totalUrls.contains(url))
					continue;
				
			    resp = client.fetchPR(URLUtil.getHost(url), ChinazCrawlerPatch.encode);
			    String pr = EntityUtils.toString(resp.getEntity());
				if (pr.length() == 0 || pr.length()>5 || pr.contains("-") )
					pr = "0";
				ChinazCrawlerPatch.totalUrls.add(url);
				sb.append(anchor).append("\t").append(pr).append("\n");
			}
			ChinazCrawlerPatch.crawledUrls.add(url);
			ChinazCrawlerPatch.crawledRecords.put(url);
			ChinazCrawlerPatch.crawledInfo();
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return sb.toString().toLowerCase();
	}
}
