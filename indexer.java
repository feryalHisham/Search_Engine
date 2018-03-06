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
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import com.trigonic.jrobotx.RobotExclusion;
import mpi.*;

public class indexer implements Serializable{
	
	
	public indexer()
	{
		
	}
	
	public void start_indexer(URL url,Document d) throws ClassNotFoundException{
		
		recv_from_crawler( url, d);
		
	}
	public void recv_from_crawler(URL url,Document d) throws ClassNotFoundException
    {
		//i think hn7tag nbreak 
		while(true) {
		byte[] yourBytes_url= new byte[10000];
		int document_size=0;
		URL url_from_crawler=null;
		Document doc_from_crawler=null;
		//10000 is assumed to be max url size
		MPI.COMM_WORLD.Recv(yourBytes_url,0,10000,MPI.BYTE,0,0);
	
		//MPI.COMM_WORLD.Recv(document_size,0,1,MPI.INT,0,1);
		//byte[] yourBytes_doc= new byte[document_size];
		//MPI.COMM_WORLD.Recv(yourBytes_doc,0,document_size,MPI.BYTE,0,2);
		
		
		
		//Create object from bytes
		ByteArrayInputStream bis = new ByteArrayInputStream(yourBytes_url);
		ObjectInput in = null;
		try {
		  in = new ObjectInputStream(bis);
		  url_from_crawler = (URL) in.readObject(); 
		  System.out.println("url_from_crawler ----> "+url_from_crawler.toString());
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
		
		
		try {
			read_document(url_from_crawler.toString(),d);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
			// Create object from bytes
			/*ByteArrayInputStream bis2 = new ByteArrayInputStream(yourBytes_doc);
			ObjectInput in2 = null;
			try {
				in2 = new ObjectInputStream(bis2);
				doc_from_crawler = (Document) in2.readObject();
				System.out.println("doc_from_crawler ----> " + doc_from_crawler.toString().length());
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
			}*/
		
    }
    }

	private void read_document(String url, Document d) throws IOException {
		// TODO Auto-generated method stub
		
		File input = new File(url+".html");
		d = Jsoup.parse(input, "UTF-8");
	}
	

}
