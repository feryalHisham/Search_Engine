import com.mongodb.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.BulkWriteError;
import com.mongodb.util.JSON;
import org.bson.*;
import org.bson.types.ObjectId;

public class dbInterface {

    DB db = null;
    LinkedList<BasicDBObject> wordsFirstinserted;

    BulkWriteOperation insertBuilder;
    DBCollection collection;

    dbInterface(String DBname, String DBCollection) {
        wordsFirstinserted = new LinkedList<>();

        try {

            MongoClient mongoClient = new MongoClient("localhost", 27017);
            db = mongoClient.getDB(DBname);
            System.out.println("Connected to Database");

        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("Server is ready ");
        collection = db.getCollection(DBCollection);
        BasicDBObject index= new BasicDBObject("stemmedWord",1);
        collection.createIndex(index,null,true);

    }

    public void insertData(Map.Entry<String, DatabaseComm> originalWords, String url) {
        BasicDBObject toInsert = new BasicDBObject();

        toInsert.put("stemmedWord", originalWords.getKey());
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

//        wordsFirstinserted.add(toInsert);
        insertBuilder = collection.initializeUnorderedBulkOperation();
        insertBuilder.insert(toInsert);

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
    // Gets the ID from error msg from duplicate key exception
    String findStemmedWord(String errormsg)
    {
        Matcher m = Pattern.compile("[\"]([^\"]*)[\"]").matcher(errormsg);  //("[0-9a-f]{24}")
        m.find();
        return m.group().replace("\"","");
    }


    void duplicateKeyHandler(MongoException e ){

            int insertionListSize= wordsFirstinserted.size();
            if(insertionListSize==0)
                return;
            String erroredStemmedWord=findStemmedWord(e.getMessage());

            for (int i = 0; i < insertionListSize; ++i)  {

                if(wordsFirstinserted.getFirst().get("stemmedWord").equals(erroredStemmedWord))
                {

                        //TODO: CAll update for the held object and insert to the rest of the list

                        //b7oto fe array b2a
                        BasicDBObject tempisa = new BasicDBObject();

                        String temp11 = wordsFirstinserted.getFirst().get("words").toString();
                        String s1 = temp11.substring(1,temp11.length()-1);
//                        System.out.println(s1);
                        BasicDBObject dbObject =  (BasicDBObject) JSON.parse(s1);


                        tempisa.put("$addToSet", new BasicDBObject().append("words",dbObject)); //mmkn tb2a $push

                        collection.update(new BasicDBObject().append("stemmedWord", erroredStemmedWord),tempisa);
                        wordsFirstinserted.removeFirst();
                        try {
                            if (wordsFirstinserted.equals(null))
                                return;
                            collection.insert(wordsFirstinserted);
                        }catch (MongoException er){
                            if (er.getCode() == 11000)
                                duplicateKeyHandler(er);

                        }
                        break;


                }
//                System.out.println(wordsFirstinserted.getFirst().get("stemmedWord"));

                wordsFirstinserted.removeFirst();    // ftema 5awafaaaaaaaaaaaaa

            }
    }
    public void initDB(/*String DBname,String DBCollection,*/ Map<String, Map<String, DatabaseComm>> interConnection, String url, boolean recrawl) {


        if (recrawl)
            recrawll(url, collection);

        for (Map.Entry<String, Map<String, DatabaseComm>> stemmedword : interConnection.entrySet()) {
            BasicDBObject theWord = new BasicDBObject();
            theWord.put("stemmedWord", stemmedword.getKey());


            //check for the word to be inserted
            DBCursor dbCursor = collection.find(theWord);
            if (dbCursor.hasNext()) {
                updateStemmedWord(collection, url, stemmedword.getValue(), stemmedword.getKey());


            } else {
                //The stemmed word itself doesn't exist
                for (Map.Entry<String, DatabaseComm> originalWords : stemmedword.getValue().entrySet()) {
                    insertData(originalWords, url);
                }
            }

        }
        //System.out.println("----------->"+wordsFirstinserted);
        try {
            collection.insert(wordsFirstinserted);
        }catch(MongoException e){
            if (e.getCode() == 11000)
                duplicateKeyHandler(e);

        }


    }


    public Vector<DatabaseComm>  findByStemmedWord(String stemmedWord){
        Vector<DatabaseComm> originalWordsInfo = new Vector<DatabaseComm>();

        BasicDBObject toFind = new BasicDBObject();

        toFind.put("stemmedWord",stemmedWord );
        DBCursor wordsFound= collection.find(toFind);
        if(wordsFound.hasNext()){
            BasicDBObject stemmedWordObj=(BasicDBObject) wordsFound.next();
            BasicDBList  wordsList= (BasicDBList) stemmedWordObj.get("words");
            if(wordsList!=null){
                Iterator<Object> wordsIterator= wordsList.iterator();
                while (wordsIterator.hasNext()) {
                    BasicDBObject wordObj = (BasicDBObject) wordsIterator.next();
                    DatabaseComm originalWordStructure = new DatabaseComm(Integer.getInteger(wordObj.get("tf").toString()),
                                    wordObj.get("tag").toString(),
                                    wordObj.get("originalWord").toString(),
                            (List<Integer>) wordObj.get("positions"),
                                    wordObj.get("url").toString());

                    originalWordsInfo.add(originalWordStructure);
                }

            }
        }

        return originalWordsInfo;

    }
}



