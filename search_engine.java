import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.omg.CORBA.portable.InputStream;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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

public class search_engine {
	
	public static void main(String[] args) throws InterruptedException, ClassNotFoundException {
		// TODO Auto-generated method stub

		MPI.Init(args);
		Integer rank=MPI.COMM_WORLD.Rank();
		Integer size=MPI.COMM_WORLD.Size();
		
		if(rank==0)
		{
			//crawler
			//handelo eh ? 
			WebCrawlerWithDepth my_webcrawl = new WebCrawlerWithDepth(5);
			my_webcrawl.start_crawler();
		}
		else if(rank==1)
		{
			//indexer
			indexer my_indexer= new indexer();
			URL dummy_url=null;
			Document dummy_doc=null;
			my_indexer.start_indexer(dummy_url,dummy_doc);
		}
		
		MPI.Finalize();
	}

}