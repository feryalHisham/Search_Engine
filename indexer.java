


import com.mongodb.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.xml.transform.TransformerException;

import mpi.*;

public class indexer implements Serializable,Runnable{
	
	 static MongoClient mongoClient ;
	 static DB database ;
	 Object lock;
	 URL url;
	 Document d;
	 boolean is_Recrawling=false;
	 boolean first=true;
	 long startTime;
	 int no_of_threads;
	 static Request req;
	public indexer(int n)
	{
		no_of_threads=n;
	}
	public indexer(Object o)
	{
		lock=o;
	}
	

	public void start_indexer() throws ClassNotFoundException{
		
		startTime = System.nanoTime();
		
		try {
			mongoClient = new MongoClient();
		    database = mongoClient.getDB("search_engine5");
		} catch (MongoException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	
		
		/*int[] end=new int[1];
		
		req=MPI.COMM_WORLD.Irecv(end, 0, 1, MPI.INT,0,0);*/
		
		Object o=new Object();
		Thread[] threads = new Thread[no_of_threads];
		for (Integer i = 1; i <= no_of_threads; i++) {
			Thread t1 = new Thread(new indexer(o));
			t1.setName(i.toString());
			t1.start();
			threads[i - 1] = t1;
		}
	
		for (int i = 0; i < threads.length; i++)
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	
		
	}


	public void recv_from_crawler() throws ClassNotFoundException
    {
		//i think hn7tag nbreak 
		
		
		byte[] yourBytes_url= new byte[10000];
		
		
	
		//10000 is assumed to be max url size
		Status status=MPI.COMM_WORLD.Recv(yourBytes_url,0,10000,MPI.BYTE,0,MPI.ANY_TAG);
	    System.out.println("now indexer start........");
		
		if(status.tag==1)
		{
			is_Recrawling=true;
			if(first)
			{
				first=false;
				System.out.println("reindexing------------");
			}
			
		}
			
	
		
		//Create object from bytes
		ByteArrayInputStream bis = new ByteArrayInputStream(yourBytes_url);
		ObjectInput in = null;
		try {
		  in = new ObjectInputStream(bis);
		  url = (URL) in.readObject(); 
		// System.out.println("url_from_crawler 1 ----> "+url.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		  try {
		    if (in != null) {
		    	
		      in.close();
		    }
		  } catch (IOException ex) {
		    // ignore close exception
		  }
		}
		
		
	
		//get_document_from_db();
		
    }

	private void get_document_from_db() {
		// TODO Auto-generated method stub
		DBCollection collection = database.getCollection("url");
		DBCursor cursor = collection.find(new BasicDBObject("url_name", url.toString()),new BasicDBObject("document",1));
		
		//---?? leeh de while
		String doc_from_db = null;
		while(cursor.hasNext()) {
			//System.out.println("only one parent at a time for "+ key);
		     BasicDBObject object = (BasicDBObject) cursor.next();
		     doc_from_db = object.getString("document");
		    
		}
		//to do
	
		//Parse a document from a String
		d= Jsoup.parse(doc_from_db);
		
		//System.out.println("document at indexer---->\n"+d);
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
         while(true) {
			
	
        	 
			synchronized(lock)
			{
				try {
					recv_from_crawler();
					System.out.println("url from crawler 2--->"+url.toString()+ " thread---> "+Thread.currentThread().getName());

				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
	
			try{
				
				get_document_from_db();
	            textTags.indexing(d,url.toString(),is_Recrawling);
	        }catch (IOException e){
			    System.out.println(e);
	        }
	
			// System.out.println("time indexer for one doc--->"+(System.nanoTime()-startTime));
		    //System.out.println("total time indexing--->"+(System.nanoTime()-startTime));

		}
		
	}
	}

	
	


