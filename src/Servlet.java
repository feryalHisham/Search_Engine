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
            processingQuery=new queryProcessing(searchString);
            processingQuery.retreiveSearchWordsInfo();
            System.out.println(searchString);
            for (Map.Entry<String,Vector<DatabaseComm>> wordsInfoMapEntry: processingQuery.wordsToRanker.entrySet() ) {

                System.out.println("search query "+wordsInfoMapEntry.getKey());
                for (  DatabaseComm  wordInfo :wordsInfoMapEntry.getValue()){
                    System.out.println("info:");
                    System.out.println("Original Word: "+wordInfo.getTheWord());
                    System.out.println("URL: "+wordInfo.getUrl());
                    System.out.println("TF "+wordInfo.getOccurence());
                    System.out.println("Tag: "+wordInfo.getTag());
                }

            }
//            PrintWriter out = response.getWriter();
//            String title = "Search Engine";
//            String docType =
//                    "<!doctype html public \"-//w3c//dtd html 4.0 " +
//                            "transitional//en\">\n";
//            out.println(docType +
//                    "<html>\n" +
//                    "<head><title>" + title + "</title></head>\n" +
//                    "<body bgcolor=\"#f0f0f0\">\n" +
//                    "<h1 align=\"center\">" + title + "</h1>\n" +
//                    "<ul>\n" +
//                    "  <li><b>Search Words</b>: "
//                    + searchString + "\n" +
//                    "</ul>\n" +
//                    "<ul>\n" +
//
//                    "  <li><b>Search Words</b>: "
//                    + request.getParameter("searchWords") + "\n" +
//                    "</ul>\n" +
//                    "</body></html>");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try{
        doGet(request, response);
        }catch (Exception e){

            out.println(e);

        }
    }
}
