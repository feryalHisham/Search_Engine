

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

public class textTags1 {

    public static stopORstem checkStopWord;
    static List<DBObject> newWords=new ArrayList<DBObject>();





    public static void main(String[] args) throws IOException {

        checkStopWord=new stopORstem();
        textTags1 teTags=new textTags1();
        int updateBulk=0;
        String file="test2.html"; //get from url
        //unique for the check on the whole txt afterwards
        final String[] neededTags={"h1","h2", "h3", "h4", "h5", "h6"};

        BufferedReader reader = new BufferedReader(new FileReader (file));
        String line,getIt="",url="";


//        FileWriter outstream= new FileWriter ("outb2a.txt");

        Map<String,DatabaseComm> objToInsert=new HashMap<String,DatabaseComm>();
        DB db=null;

        try {

            MongoClient mongoClient = new MongoClient("localhost", 27017);
            db = mongoClient.getDB("indexerTest");
            System.out.println("Connected to Database");

        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("Server is ready ");
        DBCollection collection = db.getCollection("wordsIndex");



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
                /////////////////////////////////////////////////
        for (String tag:neededTags) {
//            outstream.write("USED TAG: "+tag+'\n');
            for (Element element : doc.select(tag)) {

                String[] words = element.text().split(" ");

                for (String word : words) {

                    word=checkStopWord.modifyWord(word,tag);
                    if(word==null)
                        continue;
                    if(word.length()==1&&word!="a")
                        continue;


                    if (! objToInsert.containsKey(word)){
                        objToInsert.put(word,new DatabaseComm());
                        objToInsert.get(word).changeTag();


                    }


//                    outstream.write(word + " ");

                }

            }
        }


        String[] words = innerBody.split(" ");
        for (int i = 0; i < words.length; i++) {
            String wordInURLObject=words[i];
            String word=words[i];

            if (objToInsert.containsKey(word))
                continue;

            word=checkStopWord.modifyWord(word,"p");
            if(word==null)
                continue;
            if(word.length()==1&&word!="a")
                continue;

//            outstream.write(word + " ");
            if (! objToInsert.containsKey(word)){
                objToInsert.put(word,new DatabaseComm());
            }

            //positions of the word in inner body
            objToInsert.get(word).addPosition(i);

        }


        for (Map.Entry<String,DatabaseComm> insert:objToInsert.entrySet()){

            BasicDBObject theWord = new BasicDBObject();
            theWord.put("word",insert.getKey());

            DBCursor dbCursor = collection.find(theWord);
            if (dbCursor.hasNext()){
                // the word is already exists in our db

                // function (true if recreawl ya3ni htms7 mn 2l database 2l url dh mn kol 2l words 2l true dh heba w feryal homa 2lli ba3tinholna
                BasicDBObject idfinc = new  BasicDBObject().append("$inc",
                        new BasicDBObject().append("idf", 1));
                collection.update(new BasicDBObject().append("word",insert.getKey()),idfinc);


                BasicDBObject urlObject = new BasicDBObject();
                urlObject.put("url", url);
                urlObject.put("tf", insert.getValue().getOccurence());
                urlObject.put("tag",insert.getValue().getTag());

                BasicDBObject tempisa = new BasicDBObject();
                tempisa.put("$addToSet", new BasicDBObject().append("urls", urlObject));


                if(updateBulk==10){
                 updateBulk=0;
                 //TODO:
                    //BulkWriteResult bulkWriteResult = collection.;

                }
//                collection.update(new BasicDBObject().append("word",insert.getKey()),tempisa);
                //TODO: update with bulk
            }
            else {
                // the word is not inserted yeeeeet
                // lets insert it b2a
                if (insert.getKey()=="")
                    continue;

                theWord.put("idf", 1);

                List<BasicDBObject> URLs = new ArrayList<>();
                BasicDBObject urlObject = new BasicDBObject();
                urlObject.put("url", url);
                urlObject.put("theword", insert.getKey());
                urlObject.put("tf", insert.getValue().getOccurence());
                urlObject.put("tag",insert.getValue().getTag());
                urlObject.put("positions",insert.getValue().getPositions());

//                URLs.add(urlObject);
//                theWord.put("urls", URLs);
                URLs.add(urlObject);
                theWord.put("urls", URLs);
                newWords. add(theWord);

            }

        }
        collection.insert(newWords);
//        outstream.close();
    }
}