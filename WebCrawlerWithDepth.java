package web_crawler_try;

import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.omg.CORBA.portable.InputStream;
import org.w3c.dom.Node;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;

import com.google.common.base.CharMatcher;
import com.trigonic.jrobotx.RobotExclusion;
import mpi.*;


public class WebCrawlerWithDepth implements Runnable,Serializable {
    private static final int MAX_DEPTH = 5;
    //public static HashSet<String> links= new HashSet<>();
    public static ConcurrentHashMap<String,Vector<String>> links= new ConcurrentHashMap<String,Vector<String>>();
    private String start_url;
    boolean to_enter;
    static FileWriter  fileWriter;
    static PrintWriter printWriter;
    static FileWriter  fileWriter_doc;
    static PrintWriter printWriter_doc;
    
    
    @Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			getPageLinks(start_url, 0,"no parent");
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
    public WebCrawlerWithDepth(String start_url) {
    	this.start_url = start_url;
    	
        //links = new HashSet<>();
        
    }
    
    //i think ya3ny enaha hta5od esm el file bta3 el seed we 3dd el threads elly hn2rah mn el user fel
    // search_engine class
    public WebCrawlerWithDepth(String seed_file, int threads_no) {
    	
    	
    	
    }
    
    
    public boolean read_robot(String url){
    	
    	URL url_kamel=null;
    	String base="" ;
		try {
			url_kamel = new URL(url);
			//System.out.println("url kamel : "+url_kamel);
			base = url_kamel.getProtocol() + "://" + url_kamel.getHost();
			//System.out.println("base : "+url_kamel);

		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	
        BufferedReader in;
		try {
			URL temp=new URL(base + "/robots.txt");
			 
		  	RobotExclusion robotExclusion = new RobotExclusion();
		    boolean is_allowed=robotExclusion.allows(url_kamel, "*");
		    //System.out.println("is robot allowed "+is_allowed);
		    return is_allowed; 
		      
		       
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			
			//can't find robot.txt
			return true;
		}
       
   
       
 
    }
   
    public static void write_links_tofile() {
    	
    	for (String key : links.keySet()) {
    		
    		for(String value: links.get(key)) {
    			printWriter.print(key+" VALUE "+value+'\n');
    		}
    	}
    }
    public void write_document(String url,Document d) throws IOException
    {
    	final CharMatcher ALNUM =
    			  CharMatcher.inRange('a', 'z').or(CharMatcher.inRange('A', 'Z'))
    			  .or(CharMatcher.inRange('0', '9')).precomputed();
    		
         String alphaAndDigits = ALNUM.retainFrom(url);
    	 fileWriter_doc = new FileWriter("documents/"+alphaAndDigits+".html");
			
		 printWriter_doc = new PrintWriter(fileWriter_doc);
		 
		 printWriter_doc.print(d);
    }
    
    public void send_to_indexer(URL url,Document d) throws TransformerException, IOException
    {
    	
		//Prepare bytes to send
    	byte[] yourBytes_url = null;
		ByteArrayOutputStream bos_url = new ByteArrayOutputStream();
		ObjectOutput out_url = null;
		
		//byte[] yourBytes_doc = null;
		//ByteArrayOutputStream bos_doc = new ByteArrayOutputStream();
		//ObjectOutput out_doc = null;
		try {
			
			out_url = new ObjectOutputStream(bos_url);   
			out_url.writeObject(url);
			out_url.flush();
			yourBytes_url = bos_url.toByteArray();
			
	      
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		  try {
		    bos_url.close();
		   // bos_doc.close();
		  } catch (IOException ex) {
		    // ignore close exception
		  }
		}
		
		
		write_document(url.toString(), d);
		MPI.COMM_WORLD.Send(yourBytes_url,0,yourBytes_url.length,MPI.BYTE,1,0);
	

    }
    
    public  void getPageLinks(String URL, int depth,String parent) throws TransformerException {
    	
    	
    	
    	//System.out.println("links size: \n"+links);
    	
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
    	 
        if (is_allowed && (links.size()<5000)&&depth<MAX_DEPTH&&(links.merge(URL,initial ,reMappingFunction)).size()==1) {
           
        	
        	
            if (URL.contains("/watch?v=")) {
            	// System.out.println("1st>> "+Thread.currentThread().getName()+">> Depth: " + depth + " [" + URL + "]");     
            	 
            }
            
            try {
            	
                //links.add(URL);
            	
                
                System.out.println("thread "+Thread.currentThread().getName()+" added 3ady"); 
                
                Document document = Jsoup.connect(URL).ignoreContentType(true).userAgent("Mozilla").get();
                
               System.out.println("sending to indexer");
               send_to_indexer(new URL(URL),document);
                
                
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
        
        
    public void start_crawler() throws InterruptedException
    {
		Thread t1 = new Thread(new WebCrawlerWithDepth("https://www.youtube.com/"));
		t1.setName("1");
		Thread t2 = new Thread(new WebCrawlerWithDepth("https://www.tutorialspoint.com"));
		t2.setName("2");
		Thread t3 = new Thread(new WebCrawlerWithDepth("https://www.geeksforgeeks.org/"));
		t3.setName("3");
		Thread t4 = new Thread(new WebCrawlerWithDepth("https://dzone.com"));
		t4.setName("4");
		Thread t5 = new Thread(new WebCrawlerWithDepth("https://www.facebook.com/"));
		t5.setName("5");
		t1.start();
		t2.start();
		t3.start();
		t4.start();
		t5.start();
		t1.join();
		t2.join();
		t3.join();
		t4.join();
		t5.join();

		System.out.println(WebCrawlerWithDepth.links.size());
		try {
	    	
	    	 System.out.println("printing in file");
			 fileWriter = new FileWriter("file_links.txt");
			
			 printWriter = new PrintWriter(fileWriter);
			 
			
			 write_links_tofile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
		/*int[] end_of_send= {1};
		MPI.COMM_WORLD.Send(end_of_send,0,1,MPI.INT,1,1);*/
		
	    printWriter.close();

            
    }
   
    
   

}