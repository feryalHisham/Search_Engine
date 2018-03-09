

import com.mongodb.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;


import java.io.*;
import java.net.UnknownHostException;
import java.util.*;

public class textTags {
    public static Set<String> docWords=new HashSet<String>();
    public static stopwords checkStopWord;
//    textTags(){
//        docWords=new HashSet<String>();
//        checkStopWord=new stopwords();
//    }

     public String modifyWord(String word,String tag){

        Stemmer porterStemmer = new Stemmer();
        word=word.toLowerCase();
        //Check if Stop word
        if(tag=="p"||tag=="span"||tag=="pre")
            if(checkStopWord.ifStopWords(word))
               return"";

        if(checkStopWord.ifCitation(word))
            return"";
            //Check if special character

         word=word.replaceAll("[^a-zA-Z0-9]", "");

        //else that: Stem the word
        porterStemmer.add(word);
        porterStemmer.stem();
        return porterStemmer.toString();

    }

    public static void main(String[] args) throws IOException {

        checkStopWord=new stopwords();
        textTags teTags=new textTags();
        String file="test2.html"; //get from url
        //unique for the check on the whole txt afterwards
        final String[] neededTags={"p","pre","span","li","h1","h2", "h3", "h4", "h5", "h6"};

        BufferedReader reader = new BufferedReader(new FileReader (file));
        String line,getIt="",url="";

        FileWriter outstream= new FileWriter ("outb2a.txt");

        Map<String,DatabaseComm> objToInsert=new HashMap<String,DatabaseComm>();


        try {
            while((line = reader.readLine()) != null) {
                getIt+=line;
                }
        } finally {
            reader.close();
        }
        final String html= getIt;
        ///////////////////////////////////////////
        Document doc = Jsoup.parse(html);
        String innerBody=doc.select("body").text();
        /////////////////////////////////////////////////
        for (String tag:neededTags) {
            outstream.write("USED TAG: "+tag+'\n');
            for (Element element : doc.select(tag)) {

                String[] words = element.text().split(" ");

                for (String word : words) {

                    docWords.add(word);
                    word=teTags.modifyWord(word,tag);
                    if (! objToInsert.containsKey(word))
                        objToInsert.put(word,new DatabaseComm());

                    objToInsert.get(word).insertWord(tag);

                    outstream.write(word + " ");

                }

            }
        }


        String[] words = innerBody.split(" ");
        for (String word : words){
            if (docWords.contains(word))
                continue;

            word=teTags.modifyWord(word,"p");
            outstream.write(word + " ");
            if (! objToInsert.containsKey(word))
                objToInsert.put(word,new DatabaseComm());

            objToInsert.get(word).insertWord("p");

        }

        DB db=null;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        try {

            MongoClient mongoClient = new MongoClient("localhost", 27017);
            db = mongoClient.getDB("indexerTest");
            System.out.println("Connected to Database");

        } catch (Exception e) {
            System.out.println(e);
        }


        System.out.println("Server is ready ");


        DBCollection collection = db.getCollection("wordsIndex");

        for (Map.Entry<String,DatabaseComm> insert:objToInsert.entrySet()){

//            BasicDBObject document = new BasicDBObject();
//            document.put("database", "mkyongDB");
//            document.put("table", "hosting");
//
//            BasicDBObject documentDetail = new BasicDBObject();
//            documentDetail.put("records", 99);
//            documentDetail.put("index", "vps_index1");
//            documentDetail.put("active", "true");
//            document.put("detail", documentDetail);
//
//            collection.insert(document);


            for (Map.Entry<String,Integer> tagsOccur:insert.getValue().getWordtags().entrySet()){

                BasicDBObject query = new BasicDBObject();
//                BasicDBObject field = new BasicDBObject();
                query.put("word", tagsOccur.getKey());
                DBCursor cursor = collection.find(query);

                if(cursor.hasNext()){
                    //TODO:get the object update the object
                    //increase idf
                    //add url with its tags and occurance to the object
                    //update the word object
                }else{
                    //TODO:create object with idf 0
                    //create object(url, its tags and occ)
                    //insert object
                }
            }
        }
            outstream.close();
    }
}
