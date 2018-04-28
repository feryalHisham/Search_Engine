import com.mongodb.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.BulkWriteError;
import com.mongodb.util.JSON;
import org.bson.*;

public class dbInterface {

    DB db = null;
    LinkedList<BasicDBObject> wordsFirstinserted;

    BulkWriteOperation insertBuilder;
    DBCollection collection;

    dbInterface(String DBCollection,DB database){
        wordsFirstinserted=new LinkedList<>();

        try {

            //MongoClient mongoClient = new MongoClient("localhost", 27017);
            db = database;
            System.out.println("Connected to Database");

        } catch (Exception e) {
            System.out.println(e);
        }
        //System.out.println("Server is ready ");
        collection = db.getCollection(DBCollection);
        //collection.createIndex("stemmedWord");

    }

    public void insertData(Map.Entry<String, DatabaseComm> originalWords, String url, String stemmedWord) {
        BasicDBObject toInsert = new BasicDBObject();

        toInsert.put("stemmedWord", stemmedWord);
        toInsert.put("idf", 1);

        List<BasicDBObject> URLs = new ArrayList<>();
        BasicDBObject wordObject = new BasicDBObject();
        wordObject.put("url", url);
        wordObject.put("originalWord", originalWords.getKey());
        wordObject.put("tf", originalWords.getValue().getOccurence());
        wordObject.put("tag", originalWords.getValue().getTag());
        wordObject.put("positions", originalWords.getValue().getPositions());
        URLs.add(wordObject);
        toInsert.put("words", URLs);

        wordsFirstinserted.add(toInsert);
//        insertBuilder = collection.initializeUnorderedBulkOperation();
//        insertBuilder.insert(toInsert);

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
    public void updateStemmedWord(DBCollection collection, String url, Map<String, DatabaseComm> interConnection, String stemmedword) {
        BasicDBObject idfinc = new BasicDBObject().append("$inc",
                new BasicDBObject().append("idf", 1));
//        collection.update(new BasicDBObject().append("word",insert.getKey()),idfinc);

        BulkWriteOperation builder = collection.initializeUnorderedBulkOperation();
        for (Map.Entry<String, DatabaseComm> originalWords : interConnection.entrySet()) {
            //da ele gwa l array words
            BasicDBObject wordObject = new BasicDBObject();
            wordObject.put("originalWord", originalWords.getKey());
            wordObject.put("url", url);
            wordObject.put("tf", originalWords.getValue().getPositions().size()); //Attention: dangerous (getOccurrence)
            wordObject.put("tag", originalWords.getValue().getTag());
            wordObject.put("positions", originalWords.getValue().getPositions());

            //b7oto fe array b2a
            BasicDBObject tempisa = new BasicDBObject();


            tempisa.put("$addToSet", new BasicDBObject().append("words", wordObject)); //mmkn tb2a $push

            builder.find(new BasicDBObject().append("stemmedWord", stemmedword)).update(tempisa);
            builder.find(new BasicDBObject().append("stemmedWord", stemmedword)).update(idfinc);
            //collection.update(new BasicDBObject().append("stemmedWord",originalWords.getKey()),tempisa);

        }
        builder.execute();
        //TODO extra isa to use it once for every url
        // another TODO decide what to do with the IDF


    }

    public void recrawll(String url, DBCollection collection) {
        BasicDBObject b1 = new BasicDBObject();
        BasicDBObject b2 = new BasicDBObject();
        BasicDBObject b3 = new BasicDBObject();
        BasicDBObject b4 = new BasicDBObject();

        //subdoc content -> uongodrl w byms7o 4la tool
        b2.put("url", url);
        b3.put("words", b2);
        b4.put("$pull", b3);

        collection.update(b1, b4, false, true);

    }
    String findStemmedWord(String errormsg)
    {
        Matcher m = Pattern.compile("[\"]([^\"]*)[\"]").matcher(errormsg);  //("[0-9a-f]{24}")
        m.find();

        if(m.equals(null))
            return "";
        return m.group().replace("\"","");
    }


    void duplicateKeyHandler(MongoException e ){

        int insertionListSize= wordsFirstinserted.size();
        if(insertionListSize==0)
            return;
        String erroredStemmedWord="";
        try{
            erroredStemmedWord=findStemmedWord(e.getMessage());

        }
        catch(IllegalStateException ee)
        {
            erroredStemmedWord="";
        }


        for (int i = 0; i < insertionListSize; ++i)  {

            if (!wordsFirstinserted.getFirst().get("stemmedWord").equals(null))
            {
                if(wordsFirstinserted.getFirst().get("stemmedWord").equals(erroredStemmedWord))
                {

                    //b7oto fe array b2a
                    BasicDBObject tempisa = new BasicDBObject();

                    String temp11 = wordsFirstinserted.getFirst().get("words").toString();
                    String s1 = temp11.substring(1,temp11.length()-1);
//        	                        System.out.println(s1);
                    BasicDBObject dbObject =  (BasicDBObject) JSON.parse(s1);


                    tempisa.put("$addToSet", new BasicDBObject().append("words",dbObject)); //mmkn tb2a $push

                    collection.update(new BasicDBObject().append("stemmedWord", erroredStemmedWord),tempisa);
                    wordsFirstinserted.removeFirst();
                    try {
                        if (wordsFirstinserted != null&&wordsFirstinserted.size()!=0){

                            //System.out.println("b-insert words");
                            collection.insert(wordsFirstinserted);
                        }
                    }catch (MongoException er){
                        if (er.getCode() == 11000)
                        {
                            //System.out.println("error---------- "+er.getCode());
                            duplicateKeyHandler(er);
                        }


                    }

                    break;

                }
            }



            wordsFirstinserted.removeFirst();


        }

//                System.out.println(wordsFirstinserted.getFirst().get("stemmedWord"));
        // ftema 5awafaaaaaaaaaaaaa

    }

    public void initDB(/*String DBname,String DBCollection,*/ Map<String, Map<String, DatabaseComm>> interConnection, String url, boolean recrawl) {


        if (recrawl)
            recrawll(url, collection);

        for (Map.Entry<String, Map<String, DatabaseComm>> stemmedword : interConnection.entrySet()) {
            BasicDBObject theWord = new BasicDBObject();
            System.out.println("stemmed word in dbInterface "+stemmedword.getKey()); //feryal
            theWord.put("stemmedWord", stemmedword.getKey());


            DBCursor dbCursor = collection.find(theWord);
            if (dbCursor.hasNext()) {
                //TODO: check for the word to be inserted
                updateStemmedWord(collection, url, stemmedword.getValue(), stemmedword.getKey());


            } else {
                //The stemmed word itself doesn't exist
                for (Map.Entry<String, DatabaseComm> originalWords : stemmedword.getValue().entrySet()) {
                    insertData(originalWords, url,stemmedword.getKey());
                }
            }

        }
        //System.out.println("----------->"+wordsFirstinserted);
        try {
            if (wordsFirstinserted != null&&wordsFirstinserted.size()!=0){

                //System.out.println("b-insert words");
                collection.insert(wordsFirstinserted);
            }
        }catch(MongoException e){
            if (e.getCode() == 11000)
            {
                //System.out.println("error---------- "+e.getCode());
                duplicateKeyHandler(e);
            }

        }




//        wordsFirstinserted.clear();
        //System.out.println("*_*_*_*_*_*"+wordsFirstinserted);

    }
}


