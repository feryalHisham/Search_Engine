import com.mongodb.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.*;

public class dbInterface {

    DB db=null;
    static List<BasicDBObject> wordsFirstinserted;

    dbInterface(){
        wordsFirstinserted=new ArrayList<>();
    }
    public void insertData(Map.Entry<String,DatabaseComm> originalWords,String url){
        BasicDBObject toInsert=new BasicDBObject();

        toInsert.put("idf", 1);

        List<BasicDBObject> URLs = new ArrayList<>();
        BasicDBObject wordObject = new BasicDBObject();
        wordObject.put("url", url);
        wordObject.put("originalWord", originalWords.getKey());
        wordObject.put("tf", originalWords.getValue().getOccurence());
        wordObject.put("tag",originalWords.getValue().getTag());
        wordObject.put("positions",originalWords.getValue().getPositions());
        URLs.add(wordObject);
        toInsert.put("words", URLs);
        wordsFirstinserted.add(toInsert);

    }
/*
* {objid:5s654,
* stemmedWord:"",
* idf:""xxx,
* words:[
*   {url:"",
*   originalWord:"",
*   tag:"",
*   tf: integer,
*   positions:[1,5,3,6]
*   },{url:"",
 *   originalWord:"",
 *   tag:"",
 *   tf: integer,
 *   positions:[1,5,3,6]
 *   },
*
*
* ]}
*
* */
    public void updateStemmedWord(DBCollection collection,String url, Map<String,DatabaseComm> interConnection,String stemmedword){
//        BasicDBObject idfinc = new  BasicDBObject().append("$inc",
//                new BasicDBObject().append("idf", 1));
//        collection.update(new BasicDBObject().append("word",insert.getKey()),idfinc);

        BulkWriteOperation builder = collection.initializeUnorderedBulkOperation();
        for (Map.Entry<String,DatabaseComm> originalWords:interConnection.entrySet()){
            //da ele gwa l array words
            BasicDBObject wordObject = new BasicDBObject();
            wordObject.put("url", url);
            wordObject.put("tf", originalWords.getValue().getPositions().size()); //Attention: dangerous (getOccurrence)
            wordObject.put("tag",originalWords.getValue().getTag());
            wordObject.put("positions",originalWords.getValue().getPositions());

            //b7oto fe array b2a
            BasicDBObject tempisa = new BasicDBObject();




            tempisa.put("$addToSet", new BasicDBObject().append("words", wordObject)); //mmkn tb2a $push

            builder.find(new BasicDBObject().append("stemmedWord",stemmedword)).update(tempisa);
            //collection.update(new BasicDBObject().append("stemmedWord",originalWords.getKey()),tempisa);

        }
        builder.execute();
        //TODO extra isa to use it once for every url
        // another TODO decide what to do with the IDF


    }
    public void recrawll ( String url,DBCollection collection){


//
//            BasicDBObject q1 = new BasicDBObject();
//            BasicDBObject q2 = new BasicDBObject();
//            BasicDBObject q3 = new BasicDBObject();
//
//            q1.put("url",url);
//            q2.put("",q1);
//            q3.put("words",q2);
//
//            DBCursor result = collection.find(q3);
//            while (result.hasNext()) {
//                BasicDBObject querry = (BasicDBObject) result.next();//new BasicDBObject("url",SURL);
//                DBObject update_idf = new BasicDBObject();
//                update_idf.put("$inc", new BasicDBObject("idf", -1));
//                collection.updateMulti(querry, update_idf);
//
//            }
            BasicDBObject b1 = new BasicDBObject();
            BasicDBObject b2 = new BasicDBObject();
            BasicDBObject b3 = new BasicDBObject();
            BasicDBObject b4 = new BasicDBObject();
            //BasicDBObject b5 = new BasicDBObject();
            //subdoc content -> uongodrl w byms7o 4la tool
            b2.put("url",url );
            b3.put("words",b2);
            b4.put("$pull",b3);
           // b5.put("multi","true");


            collection.update(b1,b4,false,true);



    }

    public void initDB(String DBname,String DBCollection, Map<String,Map<String,DatabaseComm>> interConnection,String url,boolean recrawl){
        try {

            MongoClient mongoClient = new MongoClient("localhost", 27017);
            db = mongoClient.getDB(DBname);
            System.out.println("Connected to Database");

        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("Server is ready ");
        DBCollection collection = db.getCollection(DBCollection);
        collection.createIndex("stemmedWord");

        if (recrawl)
            recrawll(url,collection);

        for (Map.Entry<String,Map<String,DatabaseComm>> stemmedword:interConnection.entrySet()){
            BasicDBObject theWord = new BasicDBObject();
            theWord.put("stemmedWord",stemmedword.getKey());

            DBCursor dbCursor = collection.find(theWord);
            if (dbCursor.hasNext()) {
                //TODO: check for the word to be inserted
                updateStemmedWord(collection,url,stemmedword.getValue(),stemmedword.getKey());


            }else{
                //The stemmed word itself doesn't exist
                for (Map.Entry<String,DatabaseComm> originalWords:stemmedword.getValue().entrySet()){
                    insertData(originalWords,url);
                }
            }

        }
        System.out.println("----------->"+wordsFirstinserted);
        collection.insert(wordsFirstinserted);

    }



}
