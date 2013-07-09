package com.hipu.seedfinder.chinaz;

import java.io.IOException;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.hipu.parser.dom.DomUtils;
import com.hipu.seedfinder.tools.ChinazCrawler;
import com.hipu.seedfinder.tools.ChinazCrawlerPatch;

public class FetchEntime extends TimerTask {
	
	public static final Logger LOG = Logger.getLogger(FetchEntime.class);
	
	LocalHttpClient client = LocalHttpClient.getInstance();

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			HttpResponse resp = client.fetchEntime();
			String content = EntityUtils.toString(resp.getEntity());
			Document doc = DomUtils.getDocument(content.getBytes(), "utf-8");
			String entime = DomParser.extractEntime("http://link.chinaz.com/", doc);
			Pattern pattern = Pattern.compile("var encode = \"(.*)\";");
			Matcher m = pattern.matcher(content);
			if (m.find())
				ChinazCrawlerPatch.encode = m.group(1);
			ChinazCrawlerPatch.entime = entime;
			LOG.info("update entime to : "+ChinazCrawlerPatch.entime+" encode to : "+ChinazCrawlerPatch.encode);
//			System.out.println(ChinazCrawler.entime+ChinazCrawler.encode);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
