
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Scanner;

import mpi.*;

public class search_engine {
	
	public static void main(String[] args) throws InterruptedException, ClassNotFoundException {
		// TODO Auto-generated method stub

		MPI.Init(args);
		Integer rank=MPI.COMM_WORLD.Rank();
		Integer size=MPI.COMM_WORLD.Size();
		
		/*System.out.println("write the no_of_threads required...");
		Scanner reader = new Scanner(System.in);  // Reading from System.in
		int n = reader.nextInt(); // Scans the next token of the input as an int.
		//once finished
		reader.close();*/
		
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
			indexer my_indexer= new indexer(5);
			
			my_indexer.start_indexer();

		}
		
		MPI.Finalize();
	}

}