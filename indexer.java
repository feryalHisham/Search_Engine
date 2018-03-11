package web_crawler_try;



import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.omg.CORBA.portable.InputStream;

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
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;

import com.google.common.base.CharMatcher;
import com.google.common.io.Files;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.trigonic.jrobotx.RobotExclusion;
import mpi.*;

public class indexer implements Serializable{
	
	 static MongoClient mongoClient ;
	 static DB database ;
	 
	public indexer()
	{
		
	}
	
	public void start_indexer(URL url,Document d) throws ClassNotFoundException{
		
		try {
			mongoClient = new MongoClient();
		    database = mongoClient.getDB("search_engine3");
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		while(true) {
		recv_from_crawler( url, d);
		}
		
	}
	public void recv_from_crawler(URL url,Document d) throws ClassNotFoundException
    {
		//i think hn7tag nbreak 
		
		byte[] yourBytes_url= new byte[10000];
		int document_size=0;
		URL url_from_crawler=null;
		Document doc_from_crawler=null;
		//10000 is assumed to be max url size
		MPI.COMM_WORLD.Recv(yourBytes_url,0,10000,MPI.BYTE,0,0);
	
		
		//Create object from bytes
		ByteArrayInputStream bis = new ByteArrayInputStream(yourBytes_url);
		ObjectInput in = null;
		try {
		  in = new ObjectInputStream(bis);
		  url_from_crawler = (URL) in.readObject(); 
		 // System.out.println("url_from_crawler ----> "+url_from_crawler.toString());
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
		
		
	
		get_document_from_db(url_from_crawler.toString(),d);
		
    }

	private void get_document_from_db(String url, Document d) {
		// TODO Auto-generated method stub
		DBCollection collection = database.getCollection("url");
		DBCursor cursor = collection.find(new BasicDBObject("url_name", url),new BasicDBObject("document",1));
		
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

	
	

}
