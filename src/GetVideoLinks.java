package web_crawler_try;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GetVideoLinks {

             //<a href="/watch?v=QNJL6nfu__Q&amp;list=PLBF29177C5D2545EF&amp;index=3" class="playlist-video clearfix  spf-link ">
    //  <a href="/watch?v=ecJymYI6MQU&amp;index=4&amp;list=RDz4ZsQCcitfE" class="playlist-video clearfix  spf-link ">

    //String outFile ="S:\\projects\\mhisoft\\youtube-link\\src\\playlist.txt";
    //BufferedWriter writer = null;
    static final String prefix = "https://www.youtube.com";

    public GetVideoLinks() {
    }
    //
    //<a href="/watch?v=4-qyuhsS9oE&amp;list=PL3ED03D57E56D7FB4&amp;index=5" class="playlist-video clearfix  spf-link ">
    //<a href="/watch?v=kdWQaecFyyo&amp;list=RDHCA9lw4hVcYwk&amp;index=2" class="playlist-video clearfix  spf-link ">


    public void parseFile(String inputFile) {
        try {
            Document doc = Jsoup.connect("https://www.youtube.com/").get();
            Elements links = doc.select("a[href]"); // a with href
            for (Element link : links) {
                
                    String url = link.attributes().get("href");
                    System.out.println(url);
                    if (url.startsWith("/watch?")) {
                        System.out.println(prefix + url);
                      
                    }
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
       
    }


    static String lowQualCmd1 = "z:\\app\\youtube-dl\\youtube-dl.exe -x --audio-format mp3 --audio-quality 128K --no-overwrites -i -a playlist.txt";
    static String highQualCmd = "z:\\app\\youtube-dl\\youtube-dl.exe -x --audio-format mp3 --audio-quality 0 --no-overwrites -i -a playlist.txt";

    public static void main(String[] args) throws IOException {
       /* final GetVideoLinks getVideoLinks = new GetVideoLinks();
        getVideoLinks.parseFile("h");*/
       /* String inputFile = "S:\\projects\\mhisoft\\youtube-link\\src\\youtube-html.txt";
        getVideoLinks.parseFile(inputFile);
        System.out.println("128bit audio download");
        System.out.println("\t"+lowQualCmd1);
        System.out.println("High quality audio download:");
        System.out.println(highQualCmd);*/
    	
    	
    	Document BeInspired = Jsoup.connect("https://www.billboard.com/").get();
        Elements links = BeInspired.select("a[href]");

        for (Element link : links) {
            if (link.attr("href").contains("/video")) {
               
            	  System.out.println(link.attr("href"));
            }

          //  System.out.println(link.attr("href"));
        }
    	
    }
}
