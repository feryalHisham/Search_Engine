
import java.util.Timer;
import java.util.TimerTask;

import org.jsoup.Jsoup;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.omg.CORBA.portable.InputStream;
import org.w3c.dom.Node;

import java.awt.List;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
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
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;
import com.trigonic.jrobotx.RobotExclusion;
import mpi.*;

public class WebCrawlerWithDepth implements Runnable, Serializable {
	// public static HashSet<String> links= new HashSet<>();
	public static ConcurrentHashMap<String, Vector<String>> links = new ConcurrentHashMap<String, Vector<String>>();
	private String start_url;
	boolean to_enter;

	int no_of_threads;
	static MongoClient mongoClient;
	static DB database;

	Timer timer;
	TimerTask timerTask;

	Object lock;
	int counter = 0;
	static Double priority =1.0;
	static int count_map=0;
	static int max_links = 200;
	// concurrent queue for synch.
	// queue of pair of url and its parent url
	public static ConcurrentLinkedQueue<Pair<String, String>> unvisited = new ConcurrentLinkedQueue<Pair<String, String>>();

	@Override
	public void run() {
		// TODO Auto-generated method stub
		// System.out.println("thread
		// no---->"+Integer.parseInt(Thread.currentThread().getName()));
		if (Integer.parseInt(Thread.currentThread().getName()) <= no_of_threads) {
			// System.out.println("now crawl----");

			try {
				// getPageLinks(start_url, 0,"no parent");
				crawl();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		} else {
			// System.out.println("now recrawl----");

			try {

				recrawl();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}

	}

	public WebCrawlerWithDepth() {

	}

	// constructor takes number of of threads read from user in search_engine
	// class
	
	public WebCrawlerWithDepth(int threads_no) {

		
		no_of_threads = threads_no;
      
	}
	
	//object o is used for sync
	public WebCrawlerWithDepth(int threads_no,Object o) {

		
		no_of_threads = threads_no;
        lock=o;
	}

	public void read_seedset() {
		BufferedReader seedStream = null;
		try {
			seedStream = new BufferedReader(new InputStreamReader(new FileInputStream("seedset.txt")));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		;

		String line;

		try {
			while ((line = seedStream.readLine()) != null) {
				unvisited.add(new Pair<String, String>(line, "no parent"));
			}
			seedStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public boolean read_robot_manual(String url) {

		URL url_kamel = null;
		// host name only part of the url ex: https://www.facebook.com
		String base = "";
		try {
			url_kamel = new URL(url);
			// System.out.println("url kamel : "+url_kamel);
			base = url_kamel.getProtocol() + "://" + url_kamel.getHost();
			// System.out.println("base : "+url_kamel);

		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			// e1.printStackTrace();
		}

		try {
			URL urlRobot = new URL(base + "/robots.txt");

			// read the robot.txt
			// String strCommands="";
			ArrayList<String> split = new ArrayList<String>();
			try {
				BufferedReader urlRobotStream = new BufferedReader(new InputStreamReader(urlRobot.openStream()));
				;

				String line;

				while ((line = urlRobotStream.readLine()) != null) {
					split.add(line);
				}
				urlRobotStream.close();

				/*
				 * byte b[] = new byte[1000]; int numRead =
				 * urlRobotStream.read(b); strCommands = new String(b, 0,
				 * numRead); while (numRead != -1) { numRead =
				 * urlRobotStream.read(b); if (numRead != -1) { String
				 * newCommands = new String(b, 0, numRead); strCommands +=
				 * newCommands; } } urlRobotStream.close();
				 */
			} catch (IOException e) {
				return true; // if there is no robots.txt file, it is OK to
								// search
			}

			ArrayList<RobotRule> robotRules = new ArrayList<>();
			String mostRecentUserAgent = null;
			for (int i = 0; i < split.size(); i++) {
				String line = split.get(i).trim();
				if (line.toLowerCase().startsWith("user-agent")) {
					int start = line.indexOf(":") + 1;
					int end = line.length();
					mostRecentUserAgent = line.substring(start, end).trim();
				} else if (line.startsWith("Disallow")) {
					if (mostRecentUserAgent != null && mostRecentUserAgent.equals("*")) {
						RobotRule r = new RobotRule();
						r.userAgent = mostRecentUserAgent;
						int start = line.indexOf(":") + 1;
						int end = line.length();
						r.rule = line.substring(start, end).trim();
						robotRules.add(r);
					}
				}
			}

			for (RobotRule robotRule : robotRules) {
				String path = url_kamel.getPath();
				if (robotRule.rule.length() == 0)
					return true; // allows everything if BLANK
				if (robotRule.rule == "/")
					return false; // allows nothing if /

				if (robotRule.rule.length() <= path.length()) {
					String pathCompare = path.substring(0, robotRule.rule.length());
					if (pathCompare.equals(robotRule.rule))
						return false;
				}
			}

			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();

			// can't find robot.txt
			return true;
		}

	}

	// parser the robots.txt and returns whether it is allowed to parse this
	// page or not
	public boolean read_robot(String url) {

		URL url_kamel = null;
		// host name only part of the url ex: https://www.facebook.com
		String base = "";
		try {
			url_kamel = new URL(url);
			// System.out.println("url kamel : "+url_kamel);
			base = url_kamel.getProtocol() + "://" + url_kamel.getHost();
			// System.out.println("base : "+url_kamel);

		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			// e1.printStackTrace();
		}

		BufferedReader in;
		try {
			URL temp = new URL(base + "/robots.txt");

			// library that parses the robots.txt
			RobotExclusion robotExclusion = new RobotExclusion();
			// robotExclusion.allows takes the whole url and returns if it is
			// allowed
			boolean is_allowed = robotExclusion.allows(url_kamel, "*");
			// System.out.println("is robot allowed "+is_allowed);
			return is_allowed;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();

			// can't find robot.txt
			return true;
		}

	}

	// uses MPI to send the URLs to the indexer
	// the indexer will receive this url and read its corresponding document
	// from the DB
	public void send_to_indexer(URL url, Document d, int crawling) throws TransformerException, IOException {

		// Prepare bytes to send
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
			// e.printStackTrace();
		} finally {
			try {
				bos_url.close();
				// bos_doc.close();
			} catch (IOException ex) {
				// ignore close exception
			}
		}

		// send the url after it's document inserted in db
		// write_document(url.toString(), d);
		Request request = MPI.COMM_WORLD.Isend(yourBytes_url, 0, yourBytes_url.length, MPI.BYTE, 1, crawling);

		/*
		 * if(request.Test().Test_cancelled()) {
		 * System.out.println("request cancelled..."); }
		 */

	}

	public void send_to_indexer_end() throws TransformerException, IOException {

		int[] end = new int[1];
		end[0] = 5;
		MPI.COMM_WORLD.Send(end, 0, 1, MPI.INT, 1, 0);

	}

	/*
	 * BFS Routine that: 1.pops the unvisited links form the queue (unvisited)
	 * which is initialized with the seed set 2.parses the Document using Jsoup
	 * 3.selects the links from the document and pushes them in the
	 * queue(unvisited) 4.checks whether the link is a video or not 5. 6. 7.
	 * 
	 */
	public void crawl() throws IOException, TransformerException {
		// unvisited.add(new Pair<String,String>(start_url,"no parent"));
		while (!unvisited.isEmpty()) {
			Pair<String, String> URL = unvisited.poll();

			// Find only almost 100 websites.
			if (links.size() > max_links) {
				System.out.println("i am thread "+Thread.currentThread().getName()+"---return size>max");
				return;
			}
			parse_and_insert_while_crawling(URL, null, false, 0);
		}
		
		if(unvisited.isEmpty())
	        System.out.println("i am thread "+Thread.currentThread().getName()+"---return empty queue");


	}

	
	void insert_map_in_db() {
		DBCollection collection = database.getCollection("url");
		System.out.println("collec " + collection);
		int actual_in_links;
		BasicDBObject searchQuery;
		for (String key : links.keySet()) {
			actual_in_links = 0;
			searchQuery = new BasicDBObject().append("url_name", key);
			for (String value : links.get(key)) {

				if (!(value.equals("no parent") || value.equals("same parent"))) {

					actual_in_links++;
					// get the id of the parent url by its name and selects only
					// the field url_name to return
					DBCursor cursor = collection.find(new BasicDBObject("url_name", value),
							new BasicDBObject("url_name", 1));

					
					while (cursor.hasNext()) {
						// System.out.println("only one parent at a time for "+
						// key);
						BasicDBObject object = (BasicDBObject) cursor.next();
						// String parenturl_id = object.getString("url_name");

						// append the parenturl_id to the url which is the key
						// in the map (and it already exists in the DB)
						BasicDBObject newDocument = new BasicDBObject();
						newDocument.append("$addToSet",
								new BasicDBObject().append("in_links_id", object.getObjectId("_id"))); // to
																										// be
																										// added
																										// to
																										// in_links_id

						collection.update(searchQuery, newDocument);

					}
				}
			}

			/*
			 * //to get the real object from db DBCursor cursor2 =
			 * collection.find(searchQuery); BasicDBObject object2 = null;
			 * if(cursor2.hasNext()) { object2 = (BasicDBObject) cursor2.next();
			 * } DBObject update = new BasicDBObject();
			 * 
			 * BasicDBList parent_ids = (BasicDBList)
			 * object2.get("in_links_id");
			 * 
			 * if(parent_ids!=null) {
			 * System.out.println("da5lt mra fe set in_links_no map");
			 * update.put("$set", new BasicDBObject("in_links_no",
			 * parent_ids.size()));
			 * 
			 * collection.update(searchQuery, update); }
			 */
		}
	}

	void insert_url_in_db(String url_name, String d, int out_links) {
		DBCollection collection = database.getCollection("url");

		DBCursor cursor = collection.find(new BasicDBObject("url_name", url_name));

		//link may be in database while recrawl
		
		if (!cursor.hasNext()) {
			// url not in db 

			
			BasicDBObject url = new BasicDBObject();

			url.put("url_name", url_name);
			url.put("pr", priority);
			
			synchronized(lock)
			{
				//priority++;
			}
			
			
			
			if (url_name.contains("/watch?v=") || (url_name.contains("youtube") && url_name.contains("embed")))
				url.put("is_video", true);
			else
				url.put("is_video", false);

			url.put("document", d);
			url.put("out_links_no", out_links);
			// url.put("in_links_no", 0);

			collection.insert(url);
			//System.out.println("result "+r.getN());
		} else {
			
		//	System.out.println("da5'lt 3'alt crawl....");

			// System.out.println("only one parent at a time for "+ key);
			BasicDBObject object = (BasicDBObject) cursor.next();
			
			// (and it already exists in the DB)
			BasicDBObject newDocument = new BasicDBObject();
			newDocument.append("$set",
					new BasicDBObject().append("document", d.toString()).append("out_links_no", out_links)); // to
																												// be
																												// added
																												// to
																												// in_links_id

			collection.update(object, newDocument);

		}
	}

	public void start_crawler() throws InterruptedException {

		long startTime = System.nanoTime();
		mongoClient = new MongoClient();
		database = mongoClient.getDB("search_engine10");

		initialization();

		
		  unvisited.add(new Pair<String, String>("https://www.youtube.com/",
		  "no parent")); unvisited.add(new Pair<String,
		  String>("https://www.tutorialspoint.com", "no parent"));
		  unvisited.add(new Pair<String,
		  String>("https://www.geeksforgeeks.org/", "no parent"));
		 unvisited.add(new Pair<String, String>("https://dzone.com",
		  "no parent")); unvisited.add(new Pair<String,
		 String>("https://www.facebook.com/", "no parent"));
		 
		  System.out.println("unvisited size "+unvisited.size());
		//read_seedset();
		/*
		 * timerTask = new TimerTask() {
		 * 
		 * @Override public void run() {
		 * System.out.println("TimerTask executing counter is: " + counter);
		 * counter++; write_links_toDb();
		 * 
		 * write_unvisited_toDb();
		 * 
		 * }
		 * 
		 * };
		 * 
		 * timer = new Timer("MyTimer");// create a new Timer
		 * 
		 * timer.scheduleAtFixedRate(timerTask, 0, 3000);// this line starts the
		 * // timer at the same // time its executed
		 * 
		 */

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("Shutdown Hook is running !");
				write_links_toDb();

				write_unvisited_toDb(false);
			}
		});

		// System.exit(0);
		Object o=new Object();
		Thread[] threads = new Thread[no_of_threads];
		for (Integer i = 1; i <= no_of_threads; i++) {
			Thread t1 = new Thread(new WebCrawlerWithDepth(no_of_threads,o));
			t1.setName(i.toString());
			t1.start();
			threads[i - 1] = t1;
		}

		for (int i = 0; i < threads.length; i++)
			threads[i].join();

		
		//test();
		System.out.println("priority ........ "+priority);
		System.out.println("merge ........ "+count_map);

		insert_map_in_db();
		// write_links_tofile();
		// write_unvisited_tofile();

		System.out.println("size of links after crawl-->" + WebCrawlerWithDepth.links.size());

		count_map=0;
		// while (true) {

		write_unvisited_toDb(true);

		links.clear();
		unvisited.clear();

		long endCrawlerTime = System.nanoTime();
		System.out.println("time after crawling--->" + (endCrawlerTime - startTime));

		before_recrawl();
		System.out.println("i am recrawling....");

		// add threads here
		Thread[] threads2 = new Thread[no_of_threads];
		for (Integer i = no_of_threads + 1; i <= 2 * no_of_threads; i++) {
			Thread t1 = new Thread(new WebCrawlerWithDepth(no_of_threads,o));
			t1.setName(i.toString());
			t1.start();
			threads2[i - no_of_threads - 1] = t1;
		}

		for (int i = 0; i < threads2.length; i++)
			threads2[i].join();
		
		
		System.out.println("priority ........ "+priority);
		System.out.println("merge ........ "+count_map);
		System.out.println("size of links after crawl-->" + WebCrawlerWithDepth.links.size());


		// after joining second threads
		insert_map_in_db();

		// }

		System.out.println("the End of crawler waitt..............................");

		try {
			send_to_indexer_end();
		} catch (TransformerException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("the End of crawler..............................");

		System.out.println("time between crawling and reclawling--->" + (System.nanoTime() - endCrawlerTime));
		System.out.println("total time crawling--->" + (System.nanoTime() - startTime));
		

		
		links.clear();
		unvisited.clear();

		// timer.cancel();

	}

	private void write_unvisited_toDb(boolean before_recrawl) {
		// TODO Auto-generated method stub
		DBCollection collection = database.getCollection("unvisited_links");
		BasicDBObject url;

		BasicDBObject document = new BasicDBObject();

		// Delete All documents from collection Using blank BasicDBObject
		collection.remove(document);

		int unvisited_size = unvisited.size();

		BulkWriteOperation builder = collection.initializeUnorderedBulkOperation();

		for (int i = 0; i < unvisited_size; i++) {

			if (before_recrawl && i > (0.25 * max_links))
				break;

			Pair<String, String> top = unvisited.peek();

			// da ele gwa l array words
			url = new BasicDBObject();
			if (top.getLeft() != null && top.getRight() != null) {
				url.put("link_name", top.getLeft());
				url.put("parent_name", top.getRight());
			}

			builder.insert(url);
			// collection.update(new
			// BasicDBObject().append("stemmedWord",originalWords.getKey()),tempisa);

			/*
			 * url = new BasicDBObject();
			 * 
			 * if (top.getLeft() != null && top.getRight() != null) {
			 * url.put("link_name", top.getLeft()); url.put("parent_name",
			 * top.getRight()); collection.insert(url); }
			 */

			// unvisited.add(top);

		}

		builder.execute();

	}

	private void write_links_toDb() {
		// TODO Auto-generated method stub

		DBCollection collection = database.getCollection("visited_links");
		BasicDBObject url;

		BasicDBObject document = new BasicDBObject();

		// Delete All documents from collection Using blank BasicDBObject
		collection.remove(document);

		ArrayList<String> array;
		for (String key : links.keySet()) {

			url = new BasicDBObject();
			url.put("link_name", key);

			array = new ArrayList<String>();
			for (String value : links.get(key)) {

				array.add(value);

			}
			url.put("parent_links", array);

			collection.insert(url);
		}

	}

	public void initialization() {

		System.out.println("initialization...");
		DBCollection collection = database.getCollection("visited_links");

		// get the id of the parent url by its name and selects only the field
		// url_name to return
		DBCursor cursor = collection.find();

		while (cursor.hasNext()) {

			BasicDBObject object = (BasicDBObject) cursor.next();
			String link_name = object.getString("link_name");

			BasicDBList parent_ids = (BasicDBList) object.get("in_links_id");
			Vector<ObjectId> parent_links_id = null;
			if (parent_ids != null) {
				Iterator<Object> it = parent_ids.iterator();
				parent_links_id = new Vector<ObjectId>();
				while (it.hasNext()) {
					ObjectId tid = (ObjectId) it.next();
					parent_links_id.add(tid);
				}

				Vector<String> parent_links = new Vector<String>();
				for (ObjectId parent : parent_links_id) {
					DBCursor cursor2 = collection.find(new BasicDBObject("_id", parent),
							new BasicDBObject("url_name", 1));

					while (cursor2.hasNext()) {
						// System.out.println("only one parent at a time for "+
						// key);
						BasicDBObject object2 = (BasicDBObject) cursor2.next();
						parent_links.add(object2.getString("url_name"));
					}
				}

				links.put(link_name, parent_links);

			}

		}

		DBCollection collection2 = database.getCollection("unvisited_links");

		// get the id of the parent url by its name and selects only the field
		// url_name to return
		DBCursor cursor2 = collection2.find();

		while (cursor2.hasNext()) {

			BasicDBObject object2 = (BasicDBObject) cursor2.next();
			String link_name = object2.getString("link_name");
			String parent_name = object2.getString("parent_name");
			unvisited.add(new Pair<String, String>(link_name, parent_name));

		}
	}

	public void before_recrawl() {

		DBCollection collection = database.getCollection("url");

		BasicDBObject searchQuery = new BasicDBObject().append("pr", new BasicDBObject("$lt", 0.25 * max_links));
		// get the id of the parent url by its name and selects only the field
		// url_name to return
		DBCursor cursor = collection.find(searchQuery);

		while (cursor.hasNext()) {

			BasicDBObject object = (BasicDBObject) cursor.next();
			String link_name = object.getString("url_name");

			// System.out.println("url_from_db_re--->" + link_name);

			unvisited.add(new Pair<String, String>(link_name, "no parent"));

		}

		DBCollection collection2 = database.getCollection("unvisited_links");

		// get the id of the parent url by its name and selects only the field
		// url_name to return
		DBCursor cursor2 = collection2.find().limit((int) (0.25 * max_links));

		while (cursor2.hasNext()) {

			BasicDBObject object2 = (BasicDBObject) cursor2.next();
			String link_name = object2.getString("link_name");
			String parent_name = object2.getString("parent_name");

			// System.out.println("url_from_unvisited_re--->" + link_name + " "
			// + parent_name);

			unvisited.add(new Pair<String, String>(link_name, parent_name));

		}

	}

	public void recrawl() throws IOException, TransformerException {
		DBCollection collection = database.getCollection("url");
		while (!unvisited.isEmpty()) {
			// Find only almost 100 websites.
			if (links.size() > max_links) {
				System.out.println("i am thread "+Thread.currentThread().getName()+"---return size>max");
				return;
			}

			Pair<String, String> url = unvisited.poll();

			// is url exist in db

			DBCursor cursor = collection.find(new BasicDBObject("url_name", url.getLeft()));

			if (cursor.hasNext()) {
				// yes url in db

				// System.out.println("link is in DB................\n");
				BasicDBObject object = (BasicDBObject) cursor.next();

				BasicDBList parent_ids = (BasicDBList) object.get("in_links_id");

				if (parent_ids != null) {
					Iterator<Object> it = parent_ids.iterator();
					Vector<ObjectId> parent_ids_list = new Vector<ObjectId>();
					while (it.hasNext()) {
						ObjectId tid = (ObjectId) it.next();
						parent_ids_list.add(tid);
					}

					// is_par_exist in db daymeen 3aizen bs ngeeb al id
					DBCursor cursor_par = collection.find(new BasicDBObject("url_name", url.getRight()));
					if (cursor_par.hasNext()) {
						BasicDBObject object2 = (BasicDBObject) cursor_par.next();
						if (!parent_ids_list.contains(object2.getObjectId("_id"))) {
							// System.out.println("i am not contains");
							// push in db as a parent for this url anma lw kan
							// mawgod f5las
							BasicDBObject newDocument = new BasicDBObject();
							newDocument.append("$addToSet",
									new BasicDBObject().append("in_links_id", object2.getObjectId("_id"))); // to
																											// be
							collection.update(object, newDocument); // added
							// to
							// in_links_id

							// BasicDBObject searchQuery = new
							// BasicDBObject().append("url_name",
							// url.getLeft());

							/*
							 * DBObject update_in_links_no = new
							 * BasicDBObject();
							 * 
							 * if(parent_ids!=null) { System.out.
							 * println("da5lt mra fe set in_links_no recrawl");
							 * 
							 * update_in_links_no.put("$set", new
							 * BasicDBObject("in_links_no",
							 * parent_ids.size()+1)); collection.update(object,
							 * update_in_links_no);
							 * 
							 * }
							 */

						}
					}

				}

				String doc_from_db = object.getString("document");
				Document new_document = Jsoup.connect(url.getLeft()).ignoreContentType(true).userAgent("Mozilla").get();

				if (!doc_from_db.equals(new_document.toString())) {
					// there is a change in document

					delete_url_childs_fromDB(url.getLeft());

					parse_and_insert_while_crawling(url, new_document, true, 1);
				}

			} else {
				// url not in db

				parse_and_insert_while_crawling(url, null, false, 1);
			}

		}
		
		if(unvisited.isEmpty())
	        System.out.println("i am thread "+Thread.currentThread().getName()+"---return empty queue");


	}

	private void parse_and_insert_while_crawling(Pair<String, String> URL, Document document, boolean true_doc,
			int crawling) throws TransformerException {
		// TODO Auto-generated method stub

		boolean ok = false;
		URL url = null;
		BufferedReader br = null;

		// keeps polling until reaches a correct link with no problems
		while (!ok) {
			try {
				url = new URL(URL.getLeft());
				br = new BufferedReader(new InputStreamReader(url.openStream()));
				ok = true;
			} catch (MalformedURLException e) {
				// System.out.println("\nMalformedURL : " + URL.getLeft() +
				// "\n");
				// Get next URL from queue
				if (!unvisited.isEmpty()) {
					URL = unvisited.poll();
					ok = false;
				} else
					return;

			} catch (IOException e) {
				// System.out.println("\nIOException for URL : " + URL + "\n");
				// Get next URL from queue
				if (!unvisited.isEmpty()) {
					URL = unvisited.poll();
					ok = false;
				} else
					return;
			}
		}

		// normalize the url
		URL.setLeft(Url_normalize.url_normalization(URL.getLeft()));
		// checks the robots.txt allowance
		// boolean is_allowed = read_robot(URL.getLeft());

		boolean is_allowed = read_robot_manual(URL.getLeft());

		// URL.getRight() will return the url itself
		String parent = URL.getRight();
		String parent_embed = URL.getLeft();

		// used in merge function it is called when the key already exists and
		// we need to update its value
		BiFunction<Vector<String>, Vector<String>, Vector<String>> reMappingFunction = (Vector<String> oldvec,
				Vector<String> newvec) -> {
			Vector<String> temp = new Vector<String>();
			temp.addAll(oldvec);

			if (!temp.contains(parent)) {
				temp.add(parent);
			} else
				temp.add("same parent");
			return temp;
		};

		BiFunction<Vector<String>, Vector<String>, Vector<String>> reMappingFunction2 = (Vector<String> oldvec,
				Vector<String> newvec) -> {
			Vector<String> temp = new Vector<String>();
			temp.addAll(oldvec);

			if (!temp.contains(parent_embed)) {
				temp.add(parent_embed);
			} else
				temp.add("same parent");
			return temp;
		};

		// initial value of the map key (the first parent) it is used in the
		// merge function
		Vector<String> initial = new Vector<String>();
		initial.add(URL.getRight());

		// merge function checks if the key exists if yes calls remapping
		// function if no adds the key with the initial value
		// merge returns the value of the key specified(1st param.)
		if (is_allowed ) {
			boolean enter=false;
			
			boolean get_doc=false;
			synchronized(lock)
			{
				if (!true_doc&&!links.contains(URL.getLeft()))
					get_doc=true;
			}
			
			
			try {
				
				if(get_doc)
					document = Jsoup.connect(URL.getLeft()).ignoreContentType(true).userAgent("Mozilla").get();
					
					synchronized(lock)
					{
						
					  if( (links.merge(URL.getLeft(), initial, reMappingFunction)).size() == 1)
					  {
						enter=true;
						count_map++;
					  }
					}
						

					if(enter)
					{
					
						//if (!true_doc)
						//	document = Jsoup.connect(URL.getLeft()).ignoreContentType(true).userAgent("Mozilla").get();

						Elements linksOnPage = document.select("a[href]");
						Elements linksOnPage2 = document.select("iframe");

						// printWriter.print(URL.getLeft()+(linksOnPage.size()+linksOnPage2.size())+'\n');
						// ---????

						insert_url_in_db(URL.getLeft(), document.toString(), linksOnPage.size() + linksOnPage2.size());
						send_to_indexer(new URL(URL.getLeft()), document, crawling);

						// 5. For each extracted URL... go back to Step 4.
						for (Element page : linksOnPage2) {

							Vector<String> initial2 = new Vector<String>();
							initial2.add(URL.getLeft()); // ana al parent bta3hom

							if ((page.attr("src").contains("/embed") && page.attr("src").contains("youtube")
									)) {
								
								boolean enter2=false;
								
								synchronized(lock)
								{
									if((links.merge(page.attr("src"), initial2, reMappingFunction2)).size() == 1)
										{
										enter2=true;
										count_map++;
										
										}
								}
								// Document document_video =
								// Jsoup.connect(page.attr("src")).ignoreContentType(true).userAgent("Mozilla").get();

								// 0 out_links as doesn't matter
								// a3takd al document bta3 al parent ahm laan da i_frame
								if(enter2)
								{
								insert_url_in_db(page.attr("src"), document.toString(), 0);
								send_to_indexer(new URL(page.attr("src")), document, crawling);
								}
							}
						}

						for (Element page : linksOnPage) {
							// getPageLinks(page.attr("abs:href"),URL);
							unvisited.add(new Pair<String, String>(page.attr("abs:href"), URL.getLeft()));
							synchronized(lock)
							{
								//this url is in db so add the current url to him as parent
								if(links.contains(page.attr("abs:href")))
								{
									Vector<String> v=links.get(page.attr("abs:href"));
									if(!v.contains(URL.getLeft()))
									v.add(URL.getLeft());

									links.put(page.attr("abs:href"),v);

								}

							}
						}



				}
					
				} catch (IOException e) {
					  System.err.println("For '" + URL + "': " + e.getMessage());
					} catch (UncheckedIOException e) {
					 System.err.println("For '" + URL + "': " + e.getMessage());
					}

			}
	
		}

	

	public void delete_url_childs_fromDB(String url) {
		DBCollection collection = database.getCollection("url");
		DBCursor cursor = collection.find(new BasicDBObject("url_name", url), new BasicDBObject("url_name", 1));

		while (cursor.hasNext()) {

			BasicDBObject object = (BasicDBObject) cursor.next();

			// this function deletes from table(word) from all the rows the
			// value heba from array in_links_id
			// db.student.update( { "subjects" : "maths" }, { $pull: {
			// "subjects": "maths" }} );

			BasicDBObject query = new BasicDBObject("in_links_id", object.getObjectId("_id"));

			/*
			 * DBObject update_in_links_no = new BasicDBObject();
			 * update_in_links_no.put("$inc", new BasicDBObject("in_links_no",
			 * -1)); collection.updateMulti(query, update_in_links_no);
			 */
			DBObject update = new BasicDBObject();
			update.put("$pull", new BasicDBObject("in_links_id", object.getObjectId("_id")));
			collection.updateMulti(query, update);

			/*
			 * //to remove the entire row of this url BasicDBObject document =
			 * new BasicDBObject(); document.put("url_name", url);
			 * collection.remove(document);
			 */
		}

	}

	void test()
	{
		
			DBCollection collection = database.getCollection("url");
		
			BasicDBObject searchQuery;
			int i=0;
			for (String key : links.keySet()) {
			
				searchQuery = new BasicDBObject().append("url_name", key);
				
					DBCursor cursor =collection.find(searchQuery,
							new BasicDBObject("url_name", 1));
					
					if(cursor.hasNext()) {
						i++;
					}
					else
					{
						System.out.println("no found-----------------------");
					}
				
			}
			System.out.println("no of links found "+i);
	}
}