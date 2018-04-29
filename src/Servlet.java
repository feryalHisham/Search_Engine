import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.nodes.Document;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Struct;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static java.lang.System.out;

@WebServlet(name = "Servlet",urlPatterns = {"/Servlet"})
public class Servlet extends HttpServlet {
    queryProcessing processingQuery;
    dbInterface findInDB;

    AutoComplete my_autocomp;
    boolean first=true;
    File dir;
    Directory directory;
    SpellChecker spellChecker;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean videos_only = false;

        String action = request.getParameter("action");

        Integer low_r=0,max_r=0, showRegularSearch;

        if(first)
        {
            first=false;
            findInDB=new dbInterface("search_engine10","WordsIndex");
            dir = new File("/home/heba/Documents/cmp/third_year/apt/searchEngineTagmee3/");
            directory = FSDirectory.open(dir);
            spellChecker = new SpellChecker(directory);

        }


        if (action != null && action.equals("autocomp")) {

            my_autocomp = new AutoComplete();
            String searchAutoComp = request.getParameter("userSearch");
            String suggest = my_autocomp.findPatterninDB(searchAutoComp);

            response.setContentType("text/html");


            response.getWriter().write(suggest);
           /* String[] suggestions=suggest.split(" ");
            StringBuilder displaySuggestions = new StringBuilder();

            for (String suggestion: suggestions) {
                displaySuggestions.append("\n" +
                        "                            <ul >\n" +" <li  class=\"hid\" ><a href=\"#\"></a>"+suggestion+"</li>\n" +
                        "                            </ul>\n" +
                        "                            ");

            }

            System.out.println("action is " + action);
            System.out.println("words are  " + searchAutoComp);
            response.getWriter().write(displaySuggestions.toString());
            */
            //System.out.println("action is " + action);
            //System.out.println("words are  " + searchAutoComp);
        }

        else if (action!= null && (action.equals("search")||action.equals("did_you_mean"))||action.equals("search2")||action.equals("search3")) {

            StringBuilder display = new StringBuilder();

            String searchString;
            if(action.equals("search")) {
                searchString = request.getParameter("userSearch");
                low_r=0;
                max_r=10;
            }else if(action.equals("search2")) {
                searchString = request.getParameter("userSearch");
                low_r=11;
                max_r=20;
            }else if(action.equals("search3")) {
                searchString = request.getParameter("userSearch");
                low_r=21;
                max_r=200;
            }
            else
            {
                searchString = request.getParameter("meanSearch");
                low_r=0;
                max_r=10;


            }


            String isVideos = request.getParameter("isvideos");

            if (isVideos.equals("true")){

                videos_only= true;
            }

            System.out.println("action is " + action);
            System.out.println("is Videos are  " + isVideos);
            System.out.println("??????????? "+searchString);



            // Set response content type
            response.setContentType("text/html");


            String title = "Search Engine";


            try {
                get_did_you_mean(searchString,display);
            } catch (IOException e) {
                e.printStackTrace();
            }



            processingQuery = new queryProcessing(searchString);
            processingQuery.retreiveSearchWordsInfo();

            System.out.println("size of map to ranker" + processingQuery.wordsToRanker + " " + processingQuery.phraseFinalToRanker);

            if(processingQuery.phraseFinalToRanker.size()>max_r && low_r==0)
            {
                showRegularSearch=0;

            }
            else
            {
                showRegularSearch=1;

            }


            show_the_results_of_phrase_search(videos_only, display);

            show_the_results_of_regular_search(videos_only, display,low_r,max_r,showRegularSearch);


            response.getWriter().write(display.toString());


        }
    }

    public void out_the_url(String url,Integer starting_snippet,StringBuilder display,Boolean is_video)
    {

        display.append( "<div class=\"col-12 col-lg-9\">\n" +
                "                    <!-- Single Result Area  -->\n" +
                "                    <div class=\"single-blog-area blog-style-2 mb-50\">\n" +
                "                        <!-- Blog Content -->\n" +
                "                        <div class=\"single-blog-content\">\n" +
                "                            <div class=\"line\"></div>\n" +
                "                            <h4><a target=\"_blank\" href=\""+url+"\" class=\"post-headline mb-0\">"+url+"</a></h4>\n" );


        Document urlDocHTML=findInDB.getDocByURLCrawlerDB(url);
        List<String> urlDocWords=Arrays.asList(urlDocHTML.select("body").text().split(" "));

       if(!url.contains("youtube"))
        {
            display.append( "<p>"
            );

            int upper= Math.min(starting_snippet+50,urlDocWords.size()-1);
            if(starting_snippet>=urlDocWords.size())
            {
                System.out.println("position bra al document....");
                starting_snippet=0;
                upper=Math.min(starting_snippet+50,urlDocWords.size()-1);
            }

            for(int i=starting_snippet;i< upper;i++)
            {
                display.append( urlDocWords.get(i) + " ");
            }

            display.append(
                    "</p>\n" +
                            "                             </div>\n" +
                            "                    </div>\n" +
                            "                </div>\n" );

        }

        else
        {
            display.append( "<p>"
            );

            display.append( "Watch the video to learn more.... ");

            display.append(
                    "</p>\n" +
                            "                             </div>\n" +
                            "                    </div>\n" +
                            "                </div>\n" );


        }

    }

    private void show_the_results_of_regular_search(boolean videos_only,StringBuilder display,Integer low_r,Integer max_r,Integer showRegularSearch) {

        Relevance_Ranker re_for_regular_words;
        Map<String,Pair<Double,Pair<Integer,Boolean>>> regular_results=null;
        if(processingQuery.wordsToRanker!=null&&processingQuery.wordsToRanker.size()!=0) {
           /* for(String word:processingQuery.wordsToRanker.keySet()) {
                for(int i=0;i<(processingQuery.wordsToRanker.get(word)).getRight().size();i++)
                {
                    System.out.println((processingQuery.wordsToRanker.get(word)).getRight().get(i).url);
                    System.out.println(i);
                }

            }*/

            re_for_regular_words = new Relevance_Ranker(processingQuery.wordsToRanker);
            regular_results = re_for_regular_words.get_pages_sorted_from_ranker();




            /*display.append( "<ul>\n" +
                    "  <li><b>Regular Results</b>:\n "
                    +
                    "</ul>\n" );*/
            int itirateValueTOshowInpage= 0;

            if (showRegularSearch == 1) {

                for (String url : regular_results.keySet()) {

                    if (itirateValueTOshowInpage>low_r){
                    Pair<Integer, Boolean> snippet_video = regular_results.get(url).getRight();
                    if (videos_only) {
                        if (snippet_video.getRight() == true)
                            out_the_url(url, snippet_video.getLeft(), display, snippet_video.getRight());

                    } else {

                        out_the_url(url, snippet_video.getLeft(), display, snippet_video.getRight());
                    }

                }
                itirateValueTOshowInpage++;

                    if ( itirateValueTOshowInpage>max_r)
                        break;

                }
            }
        }


    }

    private void show_the_results_of_phrase_search(boolean videos_only, StringBuilder display) {

        Relevance_Ranker re_for_phrase_search;
        Map<String,Pair<Double,Pair<Integer,Boolean>>> phrase_results = null;
        if(processingQuery.phraseFinalToRanker!=null&&processingQuery.phraseFinalToRanker.size()!=0)
        {
            System.out.println(" phrase---- "+processingQuery.phraseFinalToRanker.size());
            re_for_phrase_search=new Relevance_Ranker(processingQuery.phraseFinalToRanker);
            phrase_results= re_for_phrase_search.get_pages_sorted_from_ranker_phrase();

           /* display.append( "<ul>\n" +
                    "  <li><b>Phrase Results</b>:\n "
                    +
                    "</ul>\n" );*/
            for(String url:phrase_results.keySet())
            {
                Pair<Integer,Boolean> snippet_video=phrase_results.get(url).getRight();
                if(videos_only)
                {
                    if(snippet_video.getRight()==true)
                        out_the_url(url,snippet_video.getLeft(),display,snippet_video.getRight());

                }
                else
                {

                    out_the_url(url,snippet_video.getLeft(),display,snippet_video.getRight());
                }


            }


        }

    }

    public void get_did_you_mean(String searchString,StringBuilder  display) throws IOException {
        String did_you_mean=Did_you_mean.did_you_mean(searchString,spellChecker);

        display.append( "<div>\n<div>\n" +
                "  <li><b>Query_search: </b>:\n "
                + searchString + "\n"+
                "<div>\n " );

        display.append( "<br> <div>\n" +
                " <li> <b>Did_you_mean: </b>:\n "
                +"<a  style=\"cursor:pointer;color:blue;text-decoration: underline;\" id =\"did_you_mean\"  onclick=\"did_you_mean();\"> "+ did_you_mean + "\n"+
                        "</a>"+

                "</div>\n <div>\n" );
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try{
            doGet(request, response);
        }catch (Exception e){

            out.println(e);

        }
    }
}