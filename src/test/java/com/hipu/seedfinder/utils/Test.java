package com.hipu.seedfinder.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.hipu.crawlcommons.config.Config;
import com.hipu.crawlcommons.url.URLUtil;
import com.hipu.seedfinder.utils.Reflection.InnerClass;

public class Test {

	/**
	 * @throws MalformedURLException 
	 * @throws ConfigurationException 
	 * @Title: main
	 * @Description: TODO
	 * @param args 
	 * @throws
	 */
	
	private String str ;
	
	private String strs[] = new String[]{"1","2"};
	
	public Test() {
		this.str = "fasdfa";
	}
	
	
	public String[] getStrs() {
		return strs;
	}
	
	public void showStr() {
		System.out.println(strs[0]); 
	}
	
	public void deleteString(List<String> args) {
		args.remove(1);
	}
	
	 public String LongestCommonPrefix(String str1, String str2) {
	    	if ( str1 == null || str2 == null)
	    		return null;
	    	
	    	int index = 0;
	    	while( index < str1.length() && index < str2.length() && str1.charAt(index) == str2.charAt(index))
	    		index++;
	    	
	    	return str1.substring(0, index);
	    }
	 
	 public void rewrite() throws IOException {
		 String base = "D:\\workspace\\SeedFinder\\src\\main\\resources\\";
		 BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(base+"successurl2"), "utf-8"));
		 BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream(base+"third"), "utf-8"));
		 BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(base+"third2"), "utf-8"));
	        Map<String,String> records = new HashMap<String, String>();

	        String line;
	      
	        try {
				while( (line=br.readLine()) != null){
					if (line.length() < 3)
						continue;
					
				    records.put(line.split("\t")[0],line);
//	            LOG.info(records.size());
				}
				
				
				while( (line=br2.readLine()) != null){
					if (line.length() < 3)
						continue;
					wr.write(line+ records.get(line.split("\t")[4])+"\n");
//	            LOG.info(records.size());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        br.close();
	        br2.close();
	        wr.close();
	 }
	 @NewAnnotation(name="updateString",content="hello")
	 public void updateString(Test t) {
		 t.str = "hello";
	 }
	 
	 public int calculateDistance(String strA, String strB) {
		   int lenA = (int)strA.length()+1;  
		   int lenB = (int)strB.length()+1;
		   int c[][] = new int[lenA][lenB] ;
		   // Record the distance of all begin points of each string  
		   //初始化方式与背包问题有点不同  
		   for(int i = 0; i < lenA; i++) c[i][0] = i;  
		   for(int j = 0; j < lenB; j++) c[0][j] = j;  
		   c[0][0] = 0;  
		   for(int i = 1; i < lenA; i++)  
		   {  
		   for(int j = 1; j < lenB; j++)  
		   {  
		     if(strB.charAt(j-1) == strA.charAt(i-1))  
		       c[i][j] = c[i-1][j-1];  
		     else  
		       c[i][j] = Math.min(c[i][j-1], Math.min(c[i-1][j], c[i-1][j-1])) + 1;  
		   }  
		   }  
		   int ret =  c[lenA-1][lenB-1];  
		   return ret;  
	 }
	 
	 
	 
	 public void addDomain() throws IOException {
		 String base = "D:\\workspace\\SeedFinder\\src\\main\\resources\\";
		 BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(base+"lvse2"), "utf-8"));
		 BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(base+"lvses"), "utf-8"));

	        String line;
	      
	        try {
				while( (line=br.readLine()) != null){
					if (line.length() < 3)
						continue;
					String url = line.split("\t")[0];
					wr.write(URLUtil.getDomainName(url)+"\t"+line+"\n");
//	            LOG.info(records.size());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        br.close();
	        wr.close();
	 }
	 
	 public void updateEntity(Entity e) {
		 e.setI(10);
	 }
	 
	public static void main(String[] args) throws ConfigurationException, IOException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		Config conf = Config.getInstance();
        Configuration config = new PropertiesConfiguration(Test.class.getResource("/config.properties"));
        conf.registerConfig(config);
		// TODO Auto-generated method stub
//		SiteInfoDAO.getInstance().siteExist("tingche.info");
//        System.out.println(URLUtil.getHost("http://www.sina.com.cn"));
//        String str = "http://news.022ee.com";
//        System.out.println(str.substring(0, str.lastIndexOf(URLUtil.getDomainName(str))));
//        System.out.println(str.replace(URLUtil.getHost(str), ""));
//        
//        String[] arg = "12 234 3445 65 74 5 23 132 ".split(" ");
//        List<String> list = new ArrayList<String>();
//        list.add("23");
//        list.add("343");
//        list.add("332");
//        Collections.sort(list);
//        for (String strs : list) {
//        	System.out.println(strs);
//        }
        Entity e = new Entity();
        Test t = new Test();
//        t.addDomain();
        
        Reflection fl = new Reflection();
//        fl.showAnnotation(t.getClass());
//        new Test().updateString(t);
//        System.out.println(t.str);
//        
//        
//        System.out.println(Class.forName("com.hipu.seedfinder.utils.Test").getMethod("calculateDistance", String.class,String.class).invoke(t, "123", "123"));
//        
//        System.out.println(t.calculateDistance("http://news.sina.com.cn/s/2013-06-07/125027342455.shtml", "http://news.sina.com.cn/s/2013-06-07/121027342139.shtml"));
//        t.addDomain();
//        t.deleteString(list);
//        System.out.println(list.size());
        
//        String news = "http://news.022ee.com/news";
//        System.out.println(new URL(new URL(news), "action").toString());
//        
//        URLNormalizer nomal = new BasicURLNormalizer();
//        System.out.println(nomal.normalize("http://news.022ee.com/news/../action"));
//        t.rewrite();
//        String url = "http://www.baidu.com/d/../action";
//        System.out.println( new BasicURLNormalizer().normalize(url));
//        
//        Pattern pattern = Pattern.compile(".*(vat).*");
//        System.out.println(pattern.matcher("2元加盟店").matches());
//        System.out.println(pattern.matcher("2元吃饭盟店").matches());
//        
//        String str = "2034-";
//        
//        System.out.println("match"+str.matches("^[0-9]+[_-]?$"));
//        
//        ExtractUrlResult r = new ExtractUrlResult(ExtractUrlType.SUCCESS,"fs");
//        System.out.println(r.toString());
//        
//        Pattern pattern2 = Pattern.compile("var encode = \"(.*)\";");
//        Matcher m = pattern2.matcher(" var encode = \"OGG9xdOiHTQ7gm/iF91zoEXanhtEC3KL\";");
//        if (m.find())
//        	System.out.println(m.group(1));
//        
//        Pattern p = Pattern.compile("javascript:window.open\\('(.*)','_blank',''\\)");
//        Matcher mer = p.matcher("javascript:window.open('http://www.bj.cyberpolice.cn/index.htm','_blank','')");
//        if (mer.find())
//        	System.out.println(mer.group(1));
//        
//        
//        Pattern pt = Pattern.compile("(ab|bc)");
//        String strs = "123ab23454bc12";
//        Matcher matcher = pt.matcher(strs);
//        if (matcher.find()) {
//        	System.out.println(matcher.group(1));
//        	if (matcher.find())
//        		System.out.println(matcher.group(1));
//        }
//        
//        List<String> newAnchors = Lists.newArrayList();
//        newAnchors.add("http://news.sina.com.cn/s/2013-06-07/121027342139.shtml");
//        newAnchors.add("http://news.sina.com.cn/s/2013-06-07/141827343106.shtml");
//        newAnchors.add("http://news.sina.com.cn/s/2013-06-07/122427342245.shtml");
//        newAnchors.add("http://slide.news.sina.com.cn/z/slide_1_43269_3271.html");
//        newAnchors.add("http://slide.news.sina.com.cn/s/slide_1_2841_32640.html");
//        Collections.sort(newAnchors);
//        
//        int count = 1;
//		int index = 0;
//		for (int i=1; i<newAnchors.size();i++) {
//			int dis = t.calculateDistance(newAnchors.get(i-1), newAnchors.get(i));
//			System.out.println(dis);
//			if (dis < 8) {
//				count ++;
//			} else {
//				if ((1.0 * count) / newAnchors.size() >= 0.5) {
//					for (String st : newAnchors.subList(index, i)) 
//						System.out.println(st+" ");
//					System.out.println("\n");
//				}
//				index = i;
//				count = 1;
//			}
//		}
        Ihello h = new Hello();
//        java.util.concurrent.ExecutorService es = Executors.newFixedThreadPool(3);
//        System.out.println(((ThreadPoolExecutor)es).getCorePoolSize());
//        new ThreadPoolExecutor(0, 0, 0, null, null).getPoolSize();
//        System.out.println(es.getClass().getMethod("getCorePoolSize", null).invoke(es, null));
        
        tag: 
        	for (int i=0;i<4;i++) {
        		continue tag;
        	}
        
        
	}

}

class Entity{
	private int i;
	
	public Entity() {
		i = 9;
	}
	
	public void setI(int j) {
		i = j;
	}
	
	public void show() {
		System.out.println(i);
	}
}



interface Ihello {
	
}

class Hello implements Ihello {
	
}
