package web_crawler_try;

import org.jsoup.Jsoup;
import org.jsoup.UncheckedIOException;
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
import java.net.UnknownHostException;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.bson.BSONObject;
import org.bson.types.ObjectId;

import com.google.common.base.CharMatcher;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.trigonic.jrobotx.RobotExclusion;
import mpi.*;


public class WebCrawlerWithDepth implements Runnable,Serializable {
      //public static HashSet<String> links= new HashSet<>();
    public static ConcurrentHashMap<String,Vector<String>> links = new ConcurrentHashMap<String,Vector<String>>();
    private String start_url;
    boolean to_enter;
    static FileWriter  fileWriter;
    static PrintWriter printWriter;
    static FileWriter  fileWriter_url;
    static PrintWriter printWriter_url;
  
    int no_of_threads;
    static MongoClient mongoClient ;
	static DB database ;
	//concurrent queue for synch.
	//queue of pair of url and its parent url
    public static ConcurrentLinkedQueue<Pair<String,String>> unvisited = new ConcurrentLinkedQueue<Pair<String,String>>();
    
    @Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			//getPageLinks(start_url, 0,"no parent");
			bfs();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
    public WebCrawlerWithDepth() {
    
       
        
    }
    
    //constructor takes number of of threads read from user in search_engine class
    public WebCrawlerWithDepth(int threads_no) {
    	
    	no_of_threads=threads_no;
    	
    }
    
    //parser the robots.txt and returns whether it is allowed to parse this page or not
    public boolean read_robot(String url){
    	
    	
    	URL url_kamel=null;
    	//host name only part of the url ex: https://www.facebook.com 
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
			 
			//library that parses the robots.txt
		  	RobotExclusion robotExclusion = new RobotExclusion();
		  	//robotExclusion.allows takes the whole url and returns if it is allowed
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
    
   //prints the map that contains each visited url with its parents urls
    public static void write_links_tofile() {
    	
    	for (String key : links.keySet()) {
    		
    		for(String value: links.get(key)) {
    			printWriter.print(key+" VALUE "+links.get(key).size()+" "+value+'\n');
    		}
    	}
    }
    
  //prints the queue that contains each unvisited url with its parent url
    public static void write_unvisited_tofile() {
    
    while(!unvisited.isEmpty())
    {
    	
        printWriter_url.print(unvisited.poll().getLeft()+" parent "+unvisited.poll().getLeft()+'\n');
       		
    }
   
    }

    
    //uses MPI to send the URLs to the indexer 
    //the indexer will receive this url and read its corresponding document from the DB
    public void send_to_indexer(URL url,Document d) throws TransformerException, IOException
    {
    	
		//Prepare bytes to send
    	byte[] yourBytes_url = null;
		ByteArrayOutputStream bos_url = new ByteArrayOutputStream();
		ObjectOutput out_url = null;
		
		
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
		
		//send the url after it's document inserted in db
		//write_document(url.toString(), d);
		MPI.COMM_WORLD.Send(yourBytes_url,0,yourBytes_url.length,MPI.BYTE,1,0);
	

    }
    
  
    
  /*BFS Routine that:
   * 1.pops the unvisited links form the queue (unvisited) which is initialized with the seed set
   * 2.parses the Document using Jsoup 
   * 3.selects the links from the document and pushes them in the queue(unvisited)
   * 4.checks whether the link is a video or not 
   * 5.
   * 6.
   * 7.
   * 
    */
    public  void bfs() throws IOException, TransformerException{
        unvisited.add(new Pair<String,String>(start_url,"no parent"));
        while(! unvisited.isEmpty()){ 
        	Pair<String,String> URL =  unvisited.poll();
            
            //Find only almost 100 websites.
            if(links.size()>200)return;
           
            boolean ok = false;
            URL url = null;
            BufferedReader br = null;
            
            
            //keeps polling until reaches a correct link with no problems
            while(!ok){ 
                try{
                    url = new URL(URL.getLeft());
                    br = new BufferedReader(new InputStreamReader(url.openStream()));
                    ok = true;
                }catch(MalformedURLException e){
                    System.out.println("\nMalformedURL : "+URL.getLeft()+"\n");
                    //Get next URL from queue
                    if(!unvisited.isEmpty())
                    {
                   URL =  unvisited.poll();
                    ok = false;
                    }
                    else return;
                   
                }catch(IOException e){
                    System.out.println("\nIOException for URL : "+URL+"\n");
                    //Get next URL from queue
                    if(!unvisited.isEmpty())
                    {
                    URL =  unvisited.poll();
                    ok = false;
                    }
                    else return;
                }
            }     
            
            
        	//checks the robots.txt allowance
        	boolean is_allowed=read_robot(URL.getLeft());
       
        	//URL.getRight() will return the url itself 
        	String parent=URL.getRight();
        	
        	// used in merge function it is called when the key already exists and we need to update its value
        	BiFunction<Vector<String>, Vector<String>, Vector<String>> reMappingFunction = (Vector<String> oldvec, Vector<String> newvec) -> {
        		Vector<String> temp = new Vector<String>();
                temp.addAll(oldvec);
               
                if(!temp.contains(parent))
                { 
          	     temp.add(parent); 
                }
                else temp.add("same parent");
                return temp;
            };
            
            
            //initial value of the map key (the first parent) it is used in the merge function
            Vector<String> initial=new Vector<String>();
            initial.add(URL.getRight());
        	 
            
            //merge function checks if the key exists if yes calls remapping function if no adds the key with the initial value
            //merge returns the value of the key specified(1st param.)
            if (is_allowed &&(links.merge(URL.getLeft(),initial ,reMappingFunction)).size()==1) {
            	
            	
                    
                    try {
                    	
                      
                        
                        System.out.println("thread "+Thread.currentThread().getName()+" added 3ady"); 
                        
                    	
                    	//gets the HTML Document of the URL 
                       Document document = Jsoup.connect(URL.getLeft()).ignoreContentType(true).userAgent("Mozilla").get();
                       
                    	
                     
                        
                        //.userAgent("Mozilla") for http error fetching url ----- try this
                        //.ignoreContentType(true) for invalid content type
                        //w check alength aly foa llexception kda msh fkrah brdo
                
                        Elements linksOnPage = document.select("a[href]");
                        Elements linksOnPage2 = document.select("iframe");
                      
                        //printWriter.print(URL.getLeft()+(linksOnPage.size()+linksOnPage2.size())+'\n'); ---????
                        
                        insert_url_in_db(URL.getLeft(),document.toString(),linksOnPage.size()+linksOnPage2.size());
                        send_to_indexer(new URL(URL.getLeft()),document);
                        
                        
                        //5. For each extracted URL... go back to Step 4.
                        for (Element page : linksOnPage2) {
                        	
                  
                        	Vector<String> initial2=new Vector<String>();
                            initial2.add(URL.getLeft());  //ana al parent bta3hom
                        
                        	if((links.merge(page.attr("src"),initial2 ,reMappingFunction)).size()==1&&page.attr("src").contains("/embed")&&page.attr("src").contains("youtube"))
                        	{
                        		Document document_video = Jsoup.connect(page.attr("src")).ignoreContentType(true).userAgent("Mozilla").get();
                                 
                        		//0 out_links as doesn't matter
                        	    insert_url_in_db(page.attr("src"),document_video.toString(),0);
                        	    send_to_indexer(new URL(page.attr("src")),document_video);
                                
                        	}
                        }
                        
                      
                        for (Element page : linksOnPage) {
                         //   getPageLinks(page.attr("abs:href"),URL);
                            unvisited.add(new Pair<String,String>(page.attr("abs:href"),URL.getLeft()));
                        
                       
                            
                        }
                        
                     
                    } catch (IOException e) {
                        System.err.println("For '" + URL + "': " + e.getMessage());
                    }catch(UncheckedIOException e) {
                    	System.err.println("For '" + URL + "': " + e.getMessage());
                	}
                	
            	
            	
      
            }
        }
    }
        
    void insert_map_in_db()
    {
    	DBCollection collection = database.getCollection("url");
    	System.out.println("collec " + collection);
    	
        for (String key : links.keySet()) {
    		
    		for(String value: links.get(key)) {
    			
    			if(!(value.equals("no parent") ||  value.equals("same parent"))) {
    		
    			//get the id of the parent url by its name and selects only the field url_name to return
    			DBCursor cursor = collection.find(new BasicDBObject("url_name", value),new BasicDBObject("url_name",1));
    			
    			//---?? leeh de while
    			while(cursor.hasNext()) {
    				//System.out.println("only one parent at a time for "+ key);
    			     BasicDBObject object = (BasicDBObject) cursor.next();
    			   //  String parenturl_id = object.getString("url_name");
    			    
    			    
    			
    			//append the parenturl_id  to the url which is the key in the map (and it already exists in the DB)
    			BasicDBObject newDocument = new BasicDBObject();
    			newDocument.append("$push", new BasicDBObject().append("in_links_id", object.getObjectId("_id") ));  //to be added to in_links_id

    			BasicDBObject searchQuery = new BasicDBObject().append("url_name", key);

    			collection.update(searchQuery, newDocument);
    			}
    		}
    		}
        }
    }
    void insert_url_in_db(String url_name, String d,int out_links)
    {
    	DBCollection collection = database.getCollection("url");
    	BasicDBObject url = new BasicDBObject();
    	
    	url.put("url_name", url_name);
    	url.put("pr", 1);
    	if(url_name.contains("/watch?v=")||(url_name.contains("youtube")&&url_name.contains("embed")))
    	url.put("is_video", true);
    	else
    	url.put("is_video", false);
    	
    	url.put("document", d);
    	url.put("out_links_no", out_links);
        
    	collection.insert(url);
    }
        
    public void start_crawler() throws InterruptedException
    {
    	
    	try {
			mongoClient = new MongoClient();
		    database = mongoClient.getDB("search_engine");
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	
		
    	try {
			fileWriter = new FileWriter("link_by_link.txt");
			 printWriter = new PrintWriter(fileWriter);

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
    	
    	
       unvisited.add(new Pair<String,String>("https://www.youtube.com/","no parent"));
       unvisited.add(new Pair<String,String>("https://www.tutorialspoint.com","no parent"));
       unvisited.add(new Pair<String,String>("https://www.geeksforgeeks.org/","no parent"));
       unvisited.add(new Pair<String,String>("https://dzone.com","no parent"));
       unvisited.add(new Pair<String,String>("https://www.facebook.com/","no parent"));
       
       Thread[] threads=new Thread[no_of_threads];
       for(Integer i=1;i<=no_of_threads;i++)
       {
    	Thread t1 = new Thread(new WebCrawlerWithDepth());
   		t1.setName(i.toString());
   		t1.start();
   		threads[i-1]=t1;
       }

       for(int i = 0; i < threads.length; i++)
    	   threads[i].join();
		
	
		System.out.println(WebCrawlerWithDepth.links.size());
		
		
		
		
		insert_map_in_db();
		try {
	    	
	    	 System.out.println("printing in file");
			 fileWriter = new FileWriter("file_links.txt");
			
			 printWriter = new PrintWriter(fileWriter);
			 fileWriter_url = new FileWriter("file_unvisited.txt");
			 printWriter_url = new PrintWriter(fileWriter_url);
			 
			
			
			 write_links_tofile();
			 
		     //write_unvisited_tofile();
		     
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	
		
	    printWriter.close();
	    printWriter_url.close();

            
    }
   
    
    

}