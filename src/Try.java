package web_crawler_try;

import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import java.util.function.BiFunction;

public class Try implements Runnable {
    private static final int MAX_DEPTH = 5;
    //public static HashSet<String> links= new HashSet<>();
    public static ConcurrentHashMap<String,Vector<String>> links= new ConcurrentHashMap<String,Vector<String>>();
    private String start_url;
    public static Object o=new Object();
    boolean to_enter;
    
    @Override
	public void run() {
		// TODO Auto-generated method stub
    	
    	
    		getPageLinks(start_url, 0,"a");
    		getPageLinks(start_url, 0,"b");
    		getPageLinks(start_url, 0,"c");
    		getPageLinks(start_url, 0,"d");
    		getPageLinks(start_url, 0,"e");
    		
		
		
	}
    public Try(String start_url) {
    	this.start_url = start_url;
        //links = new HashSet<>();
        
    }

    public  void getPageLinks(String URL, int depth,String parent) {
    	
    	BiFunction<Vector<String>, Vector<String>, Vector<String>> reMappingFunction = (Vector<String> oldvec, Vector<String> newvec) -> {
    		Vector<String> temp = new Vector<String>();
            temp.addAll(oldvec);
            //temp.addAll(newvec);
            if(!temp.contains(parent))
                  { 
            	   temp.add(parent); 
                  }
            return temp;
        };
        
        Vector<String> initial=new Vector<String>();
        initial.add(parent);
    	 
        links.merge(URL,initial ,reMappingFunction);
           
    	 
      
       System.out.println("thread >>"+Thread.currentThread().getName()+" "+links.get(URL));      

        	
    }
        
        
   
    public static void main(String[] args) throws InterruptedException, UnsupportedEncodingException {
    	
    	/*WebCrawlerWithDepth x=new WebCrawlerWithDepth();
        x.getPageLinks("https://www.youtube.com/", 0);
        System.out.println(x.links.size());*/
    	
    	/*Thread t1 = new Thread (new Try("https://www.youtube.com/")); t1.setName("1");
		Thread t2 = new Thread (new Try("https://www.youtube.com/")); t2.setName("2");

		t1.start();  t2.start();
		t1.join();  
		t2.join();
	    System.out.println(Try.links.size());*/
    	
    	
    	String paramValue = "param\\with\\backslash";
    	String yourURLStr = "https://www.googletagmanager.com/ns.html?id=GTM-K25QL22" + java.net.URLEncoder.encode(paramValue, "UTF-8");
    	System.out.println(yourURLStr);
    	try {
			java.net.URL url = new java.net.URL(yourURLStr);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }

	
}