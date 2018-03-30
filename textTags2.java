

import com.mongodb.*;
import com.mongodb.bulk.BulkWriteResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;


import java.io.*;
import java.net.UnknownHostException;
import java.util.*;

public class textTags2 {

    public static stopwords checkStopWord;
    //final static String[] neededTags={"h1","h2", "h3", "h4", "h5", "h6"};
    static public dbModel runIndexerMap;
    static dbInterface dataToDB;
    textTags2(){
    runIndexerMap=new dbModel();
    dataToDB= new dbInterface();
    }


   

    public static void indexing(Document doc,String url,boolean isRecrawling)throws IOException{


        String innerBody=doc.select("body").text();

        runIndexerMap.addHeaderWords(doc);
        Queue<String> wordsofURL= new LinkedList<>(Arrays.asList(innerBody.split(" ")));
        int i=0;
        for (String word:wordsofURL){
            runIndexerMap.addToURLMap(word,i);
            ++i;
        }

        ///////////////////////////////////////////////// Interfacing with DB
        dataToDB.initDB("search_engine5","WordsIndex",runIndexerMap.getWordsMap(),url,isRecrawling);



    }



}