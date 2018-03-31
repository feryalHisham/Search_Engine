
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

    public stopwords checkStopWord;
    //final static String[] neededTags={"h1","h2", "h3", "h4", "h5", "h6"};
    public dbModel runIndexerMap;
    dbInterface dataToDB;
    textTags2(){
        runIndexerMap=new dbModel();
        dataToDB= new dbInterface("search_engine5","WordsIndex");
    }




    public  void indexing(Document doc,String url,boolean isRecrawling)throws IOException{

    	dbModel runIndexerMap=new dbModel();

        String innerBody=doc.select("body").text();

        
        runIndexerMap.addHeaderWords(doc);
        Queue<String> wordsofURL= new LinkedList<>(Arrays.asList(innerBody.split(" ")));
        int i=0;
        for (String word:wordsofURL){
            runIndexerMap.addToURLMap(word,i);
            ++i;
        }

        ///////////////////////////////////////////////// Interfacing with DB

//        synchronized (dataToDB) {
            dataToDB.initDB(runIndexerMap.getWordsMap(), url, isRecrawling);
  //      }


    }



}