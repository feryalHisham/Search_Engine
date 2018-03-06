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
import com.trigonic.jrobotx.RobotExclusion;
import mpi.*;

public class indexer implements Serializable{
	
	
	public indexer()
	{
		
	}
	
	public void start_indexer(URL url,Document d) throws ClassNotFoundException{
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
		
    }

	private void read_document(String url, Document d) throws IOException {
		// TODO Auto-generated method stub
		final CharMatcher ALNUM =
  			  CharMatcher.inRange('a', 'z').or(CharMatcher.inRange('A', 'Z'))
  			  .or(CharMatcher.inRange('0', '9')).precomputed();
  		
       String alphaAndDigits = ALNUM.retainFrom(url);
		File input = new File("documents/"+alphaAndDigits+".html");
		d = Jsoup.parse(input, "UTF-8");
	
	
		if(input.delete()){
			System.out.println(input.getName() + " is deleted!");
		}else{
			System.out.println("Delete operation is failed.");
		}
	
		 
		  /* Elements linksOnPage = d.select("a[href]");
		   System.out.println("url indexer check---->"+url +" : ");
		   for (Element page : linksOnPage) {
               System.out.println(page.attr("abs:href"));
           }*/
	}
	

}
