import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Vector;

import static java.lang.System.out;

@WebServlet(name = "mine",urlPatterns = {"/search"})
public class Servlet extends HttpServlet {
    queryProcessing processingQuery;
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Set response content type
        response.setContentType("text/html");
        String searchString=request.getParameter("searchWords");

        PrintWriter out = response.getWriter();
        String title = "Search Engine";
        String docType =
                "<!doctype html public \"-//w3c//dtd html 4.0 " +
                        "transitional//en\">\n";

        out.println(docType +
                "<html>\n" +
                "<head><title>" + title + "</title></head>\n" +
                "<body bgcolor=\"#f0f0f0\">\n" +
                "<h1 align=\"center\">" + title + "</h1>\n" );



        String did_you_mean=Did_you_mean.did_you_mean(searchString);

        out.println( "<ul>\n" +
                "  <li><b>Query_search: </b>:\n "
                + searchString + "\n"+
                "</ul>\n" );

        out.println( "<ul>\n" +
                "  <li><b>Did_you_mean: </b>:\n "
                + did_you_mean + "\n"+
                "</ul>\n" );


        processingQuery=new queryProcessing(searchString);
        processingQuery.retreiveSearchWordsInfo();

        System.out.println("size of map to ranker"+processingQuery.wordsToRanker+" "+processingQuery.phraseFinalToRanker);



        Relevance_Ranker re_for_phrase_search;
        Map<String,Pair<Double,Integer>> phrase_results = null;
        if(processingQuery.phraseFinalToRanker!=null)
        {
            re_for_phrase_search=new Relevance_Ranker(processingQuery.phraseFinalToRanker);
            phrase_results= re_for_phrase_search.get_pages_sorted_from_ranker_phrase();

            out.println( "<ul>\n" +
                    "  <li><b>Phrase Results</b>:\n "
                    +
                    "</ul>\n" );
            for(String url:phrase_results.keySet())
            {
                out.println( "<ul>\n" +
                        "  <li>\n "
                        + url + "\n" +
                        "</ul>\n" );

            }


        }


        Relevance_Ranker re_for_regular_words;
        Map<String,Pair<Double,Integer>> regular_results=null;
         if(processingQuery.wordsToRanker!=null)
         {
             re_for_regular_words=new Relevance_Ranker(processingQuery.wordsToRanker);
             regular_results=re_for_regular_words.get_pages_sorted_from_ranker();

             out.println( "<ul>\n" +
                     "  <li><b>Regular Results</b>:\n "
                   +
                     "</ul>\n" );

             for(String url:regular_results.keySet())
             {
                 out.println( "<ul>\n" +
                         "  <li>\n "
                         + url + "\n" +
                         "</ul>\n" );


             }
         }






    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try{
            doGet(request, response);
        }catch (Exception e){

            out.println(e);

        }
    }
}