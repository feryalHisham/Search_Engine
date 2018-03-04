

package com.mkyong.depthwebcrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;

public class WebCrawlerWithDepth {
    private static final int MAX_DEPTH = 10;
    public HashSet<String> links;

    public WebCrawlerWithDepth() {
        links = new HashSet<>();
    }

    public void getPageLinks(String URL, int depth) {
        if ((!links.contains(URL) && (depth < MAX_DEPTH))) {
        	 if (URL.contains("/watch?v=")) { 
        		 System.out.println(">> Depth: " + depth + " [" + URL + "]");
        	 }
            try {
                links.add(URL);
               

                Document document = Jsoup.connect(URL).get();
                Elements linksOnPage = document.select("a[href]");
                Elements videoiframe = document.select("iframe");
                
                for (Element page : videoiframe) {
                    if(!links.contains(page.attr("src")) && page.attr("src").contains("embed")) {
                    	 links.add(page.attr("src"));
                    	 System.out.println("iframe"+page.attr("src"));
                    }
                }
                
                depth++;
                
                for (Element page : linksOnPage) {
                    getPageLinks(page.attr("abs:href"), depth);
                }
                
                
            } catch (IOException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            }
        }
    }


   /* public void getPagevideos(String URL, int depth) {
        if ((!links.contains(URL) && (depth < MAX_DEPTH))) {
        	 //if (URL.contains("/watch?v=")) { 
        		 System.out.println(">> Depth: " + depth + " [" + URL + "]");
        	 //}
            try {
                links.add(URL);
               

                Document document = Jsoup.connect(URL).get();
               
                Elements videoiframe = document.select("iframe");
                depth++;
                                
                for (Element page : videoiframe) {
                    getPageLinks(page.attr("src"), depth);
                }
                
            } catch (IOException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            }
        }
    }*/

    public static void main(String[] args) {
    	WebCrawlerWithDepth crawler = new WebCrawlerWithDepth();
    	crawler.getPageLinks("https://www.youtube.com/", 0);
    	System.out.println(crawler.links.size());
        
    }
}