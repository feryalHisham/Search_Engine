package web_crawler_try;

import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class WebCrawlerWithDepth implements Runnable {
    private static final int MAX_DEPTH = 5;
    //public static HashSet<String> links= new HashSet<>();
    public static ConcurrentHashMap<String,Integer> links= new ConcurrentHashMap<String,Integer>();
    private String start_url;
    public static Object o=new Object();
    boolean to_enter;
    
    @Override
	public void run() {
		// TODO Auto-generated method stub
		getPageLinks(start_url, 0);
		
	}
    public WebCrawlerWithDepth(String start_url) {
    	this.start_url = start_url;
        //links = new HashSet<>();
        
    }

    public  void getPageLinks(String URL, int depth) {
    	
   
    	 
        if ((links.size()<5000)&&depth<MAX_DEPTH&&URL != null && URL.length() != 0&&(links.merge(URL, 1, Integer::sum) == 1 )) {
           
            if (URL.contains("/watch?v=")) {
            	 System.out.println("1st>> "+Thread.currentThread().getName()+">> Depth: " + depth + " [" + URL + "]");     
            	 
            }
            
            try {
            	
                //links.add(URL);
            	
                
                System.out.println("thread "+Thread.currentThread().getName()+" added 3ady"); 
                
                Document document = Jsoup.connect(URL).ignoreContentType(true).userAgent("Mozilla").get();
                
                //.userAgent("Mozilla") for http error fetching url ----- try this
                //.ignoreContentType(true) for invalid content type
                //w check alength aly foa llexception kda msh fkrah brdo
                //with depth = 5 gab 988 total
             
                Elements linksOnPage = document.select("a[href]");
                Elements linksOnPage2 = document.select("iframe");
              
                //5. For each extracted URL... go back to Step 4.
                for (Element page : linksOnPage2) {
                	
                
                	if((links.merge(page.attr("src"), 1, Integer::sum) == 1 )&&page.attr("src").contains("/embed"))
                	{
                		
                		System.out.println("2nd>>"+Thread.currentThread().getName()+">> Depth: " + depth + " [" + page.attr("src") + "]");      
                	}
                }
                
                depth++;
                for (Element page : linksOnPage) {
                    getPageLinks(page.attr("abs:href"), depth);
                }
                
             
            } catch (IOException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            }
        }
    }
        
        
   
    public static void main(String[] args) throws InterruptedException {
    	
    	/*WebCrawlerWithDepth x=new WebCrawlerWithDepth();
        x.getPageLinks("https://www.youtube.com/", 0);
        System.out.println(x.links.size());*/
    	
    	Thread t1 = new Thread (new WebCrawlerWithDepth("https://www.youtube.com/")); t1.setName("1");
		Thread t2 = new Thread (new WebCrawlerWithDepth("https://www.tutorialspoint.com")); t2.setName("2");
		Thread t3 = new Thread (new WebCrawlerWithDepth("https://www.geeksforgeeks.org/")); t3.setName("3");
		Thread t4 = new Thread (new WebCrawlerWithDepth("https://dzone.com")); t4.setName("4");
		Thread t5 = new Thread (new WebCrawlerWithDepth("https://www.facebook.com/")); t5.setName("5");
		t1.start();  t2.start();
		t3.start();  t4.start();
		t5.start();
		t1.join();  
		t2.join();
		t3.join();  
		t4.join();
		t5.join();  
	
	    System.out.println(WebCrawlerWithDepth.links.size());
        
    }

	
}