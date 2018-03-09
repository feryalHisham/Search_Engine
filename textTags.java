

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
//    public static Set<String> docWords=new HashSet<String>();
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

//                    docWords.add(word);
                    word=teTags.modifyWord(word,tag);
                    if(word.length()<=1&& word!="a")
                        continue;

                    if (! objToInsert.containsKey(word))
                        objToInsert.put(word,new DatabaseComm());

                    objToInsert.get(word).insertWord(tag);

                    outstream.write(word + " ");

                }

            }
        }


        String[] words = innerBody.split(" ");
        for (String word : words){
            if (objToInsert.containsKey(word))
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



            BasicDBObject theWord = new BasicDBObject();

            theWord.put("word",insert.getKey());

            DBCursor dbCursor = collection.find(theWord);
            if (dbCursor.hasNext()){
                // the word is already exists in our db

            }
            else {
                // the word isnot inserted yeeeeet
                // lets insert it b2a
                System.out.println("ana awl mra ashof l kelma d");
                theWord.put("idf", 1);
                List<BasicDBObject> occurence = new ArrayList<>();
                for (Map.Entry<String, Integer> tagsOccur : insert.getValue().getWordtags().entrySet()) {

                    BasicDBObject occurenceTag = new BasicDBObject();
                    occurenceTag.put("tagName", tagsOccur.getKey());
                    occurenceTag.put("numOccur", tagsOccur.getValue());
                    occurence.add(occurenceTag);

                }

                List<BasicDBObject> URLs = new ArrayList<>();
                BasicDBObject urlObject = new BasicDBObject();
                urlObject.put("url", url);
                urlObject.put("tf", insert.getValue().getOccurence());
                urlObject.put("occurence", occurence);

                URLs.add(urlObject);
                theWord.put("urls", URLs);

                collection.insert(theWord);
            }

            }


            outstream.close();
    }
}
