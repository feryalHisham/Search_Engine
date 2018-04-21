package web_crawler_try;


import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Try_videos {

	public Try_videos() throws IOException {
		// TODO Auto-generated constructor stub
		
		  Document BeInspired = Jsoup.connect("https://www.youtube.com/channel/UCaKZDEMDdQc8t6GzFj1_TDw/videos").get();
	        Elements links = BeInspired.select("a[href]");

	        for (Element link : links) {
	            if (link.attr("href").contains("/watch?v=")) {
	                /*if (videos.contains(link.attr("href"))) {

	                } else {
	                    videos.add(link.attr("href"));
	                }**********/
	            	  System.out.println(link.attr("href"));
	            }

	            System.out.println(link.attr("href"));
	        }
	}

}
