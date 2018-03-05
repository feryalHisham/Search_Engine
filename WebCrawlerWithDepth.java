package web_crawler_try;

import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.omg.CORBA.portable.InputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import com.trigonic.jrobotx.RobotExclusion;

public class WebCrawlerWithDepth implements Runnable {
    private static final int MAX_DEPTH = 5;
    //public static HashSet<String> links= new HashSet<>();
    public static ConcurrentHashMap<String,Vector<String>> links= new ConcurrentHashMap<String,Vector<String>>();
    private String start_url;
    boolean to_enter;
    
    @Override
	public void run() {
		// TODO Auto-generated method stub
		getPageLinks(start_url, 0,"no parent");
		
	}
    public WebCrawlerWithDepth(String start_url) {
    	this.start_url = start_url;
    	
    	
        //links = new HashSet<>();
        
    }
    
    
    public boolean read_robot(String url){
    	
    	URL url_kamel=null;
    	String base="" ;
		try {
			url_kamel = new URL(url);
			System.out.println("url kamel : "+url_kamel);
			base = url_kamel.getProtocol() + "://" + url_kamel.getHost();
			System.out.println("base : "+url_kamel);

		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	
        BufferedReader in;
		try {
			URL temp=new URL(base + "/robots.txt");
			 
		  	RobotExclusion robotExclusion = new RobotExclusion();
		    boolean is_allowed=robotExclusion.allows(url_kamel, "*");
		    System.out.println("is robot allowed "+is_allowed);
		    return is_allowed; 
		      
		       
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			
			//can't find robot.txt
			return true;
		}
       
   
       
       
       /*Pattern p = Pattern.compile("^(http(s?)://([^/]+))");
         Matcher m = p.matcher(url);
         if (m.find()) {
             System.out.println(m.group(1));
         try(BufferedReader in = new BufferedReader(
                 new InputStreamReader(new URL(m.group(1) + "/robots.txt").openStream()))) {
             String line = null;
             while((line = in.readLine()) != null) {
                 System.out.println(line);
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
         }
         return true;*/
    }
   
    
    public  void getPageLinks(String URL, int depth,String parent) {
    	
    	System.out.println("links size: \n"+links);
    	
    	if(URL != null && URL.length() != 0)
    	{
    	boolean is_allowed=read_robot(URL);
   
    	BiFunction<Vector<String>, Vector<String>, Vector<String>> reMappingFunction = (Vector<String> oldvec, Vector<String> newvec) -> {
    		Vector<String> temp = new Vector<String>();
            temp.addAll(oldvec);
            if(!temp.contains(parent))
            { 
      	     temp.add(parent); 
            }
            return temp;
        };
        
        Vector<String> initial=new Vector<String>();
        initial.add(parent);
    	 
        if (is_allowed&&(links.size()<100)&&depth<MAX_DEPTH&&(links.merge(URL,initial ,reMappingFunction)).size()==1) {
           
            if (URL.contains("/watch?v=")) {
            	// System.out.println("1st>> "+Thread.currentThread().getName()+">> Depth: " + depth + " [" + URL + "]");     
            	 
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
                	
                	Vector<String> initial2=new Vector<String>();
                    initial2.add(URL);
                
                	if((links.merge(page.attr("src"),initial2 ,reMappingFunction)).size()==1&&page.attr("src").contains("/embed"))
                	{
                		
                		//System.out.println("2nd>>"+Thread.currentThread().getName()+">> Depth: " + depth + " [" + page.attr("src") + "]");      
                	}
                }
                
                depth++;
                for (Element page : linksOnPage) {
                    getPageLinks(page.attr("abs:href"), depth,URL);
                }
                
             
            } catch (IOException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            }
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