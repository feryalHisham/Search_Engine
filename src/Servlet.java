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

@WebServlet(name = "Servlet",urlPatterns = {"/Servlet"})
public class Servlet extends HttpServlet {
    queryProcessing processingQuery;
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {



       String action= request.getParameter("action");
       String searchAutoComp = request.getParameter("userSearch");
        System.out.println("action is "+action);
        System.out.println("words are  "+searchAutoComp);


        if (action!=null && action.equals("autocomp")){

           response.setContentType("text/html");
           response.getWriter().write(searchAutoComp);
       }


        // Set response content type
else {
            response.setContentType("text/html");
            //System.out.println(request.getServletPath());
            String searchString = request.getParameter("searchWords");
            processingQuery = new queryProcessing(searchString);
            processingQuery.retreiveSearchWordsInfo();
            System.out.println(searchString);
            System.out.println("Ranker map --->   "+processingQuery.wordsToRanker.size());
            System.out.println("Ranker phrase map --->   "+processingQuery.phraseFinalToRanker.size());


//            Vector<String> v = new Vector<>();
//            v.add("a");
//
//            v.add("b");
//            v.add("c");
//
//        Vector<String> v2 = new Vector<>();
//        v2.addAll(v);
//
//        Vector<String> v3 = new Vector<>();
//        v3.add("a");
//
//        v2.retainAll(v3);
//        System.out.println(v2);


            for (Map.Entry<String, Pair<Integer, Vector<DatabaseComm>>> wordsInfoMapEntry : processingQuery.wordsToRanker.entrySet()) {

                System.out.println("word position --->   " + wordsInfoMapEntry.getValue().getLeft());

                System.out.println("word vector --->   " + wordsInfoMapEntry.getValue().getRight().size());

                //System.out.println("search query "+wordsInfoMapEntry.getKey());
                /*for (  DatabaseComm  wordInfo :wordsInfoMapEntry.getValue()){
                    System.out.println("info:");
                    System.out.println("Original Word: "+wordInfo.getTheWord());
                    System.out.println("URL: "+wordInfo.getUrl());
                    System.out.println("TF "+wordInfo.getOccurence());
                    System.out.println("Tag: "+wordInfo.getTag());
                }*/

            }

            for (Map.Entry<String, Pair<Integer, Vector<DatabaseComm>>> wordsInfoMapEntry : processingQuery.phraseFinalToRanker.entrySet()) {

                System.out.println("phrase vector --->   " + wordsInfoMapEntry.getValue().getRight().size());



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
//
//                "</body></html>");

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
