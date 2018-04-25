import com.mongodb.*;

import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.BulkWriteError;
import com.mongodb.util.JSON;
import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import org.bson.*;
import org.bson.types.ObjectId;

import javax.xml.crypto.Data;

public class dbInterface {

    DB db = null;
    LinkedList<BasicDBObject> wordsFirstinserted;

    stopORstem stemmerObject;
    BulkWriteOperation insertBuilder;
    DBCollection collection;
    public Map<String,Pair< Integer,Vector<DatabaseComm> >> searchResultFromDB,phraseSearchResultFromDB;

    dbInterface(String DBname, String DBCollection) {
        wordsFirstinserted = new LinkedList<>();
        searchResultFromDB=new HashMap<>();
        phraseSearchResultFromDB=new HashMap<>();
        stemmerObject=new stopORstem();

        try {

            MongoClient mongoClient = new MongoClient("localhost", 27017);
            db = mongoClient.getDB(DBname);
            System.out.println("Connected to Database");

        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("Server is ready ");
        collection = db.getCollection(DBCollection);
        BasicDBObject index = new BasicDBObject("stemmedWord", 1);
        collection.createIndex(index, null, true);

    }


    dbInterface(String DBCollection, DB database) {
        wordsFirstinserted = new LinkedList<>();

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
    String findStemmedWord(String errormsg) {
        Matcher m = Pattern.compile("[\"]([^\"]*)[\"]").matcher(errormsg);  //("[0-9a-f]{24}")
        m.find();
        return m.group().replace("\"", "");
    }


    void duplicateKeyHandler(MongoException e) {

        int insertionListSize = wordsFirstinserted.size();
        if (insertionListSize == 0)
            return;
        String erroredStemmedWord = findStemmedWord(e.getMessage());

        for (int i = 0; i < insertionListSize; ++i) {

            if (wordsFirstinserted.getFirst().get("stemmedWord").equals(erroredStemmedWord)) {

                //TODO: CAll update for the held object and insert to the rest of the list

                //b7oto fe array b2a
                BasicDBObject tempisa = new BasicDBObject();

                String temp11 = wordsFirstinserted.getFirst().get("words").toString();
                String s1 = temp11.substring(1, temp11.length() - 1);
//                        System.out.println(s1);
                BasicDBObject dbObject = (BasicDBObject) JSON.parse(s1);


                tempisa.put("$addToSet", new BasicDBObject().append("words", dbObject)); //mmkn tb2a $push

                collection.update(new BasicDBObject().append("stemmedWord", erroredStemmedWord), tempisa);
                wordsFirstinserted.removeFirst();
                try {
                    if (wordsFirstinserted.equals(null))
                        return;
                    collection.insert(wordsFirstinserted);
                } catch (MongoException er) {
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
        } catch (MongoException e) {
            if (e.getCode() == 11000)
                duplicateKeyHandler(e);

        }


    }


    public Vector<DatabaseComm> findByStemmedWord(String stemmedWord) {
        //stemmedWord="mathematical";
        Vector<DatabaseComm> originalWordsInfo = new Vector<DatabaseComm>();

        BasicDBObject toFind = new BasicDBObject();

        toFind.put("stemmedWord", stemmedWord);
        DBCursor wordsFound = collection.find(toFind);
        //System.out.println("Words Found.length  ---> "+wordsFound.length());

        //System.out.println("Words Found.next  ---> "+wordsFound.curr());

        if (wordsFound.size() != 0) {
            BasicDBObject stemmedWordObj = (BasicDBObject) wordsFound.next();

            BasicDBList wordsList = (BasicDBList) stemmedWordObj.get("words");
            if (wordsList != null) {
                Iterator<Object> wordsIterator = wordsList.iterator();
                while (wordsIterator.hasNext()) {
                    BasicDBObject wordObj = (BasicDBObject) wordsIterator.next();
                    DatabaseComm originalWordStructure = new DatabaseComm(Integer.parseInt(wordObj.get("tf").toString()),
                            wordObj.get("tag").toString(),
                            wordObj.get("originalWord").toString(),
                            (List<Integer>) wordObj.get("positions"),
                            wordObj.get("url").toString());

                    originalWordsInfo.add(originalWordStructure);
                    //System.out.println(originalWordsInfo.size());
                }

            }
        }

        return originalWordsInfo;

    }
    //db.getCollection('wordsIndex').find({words:{$elemMatch:{originalWord:{$in:["stacking","stacks"]}}}})
//    db.getCollection('wordsIndex').find({words:{$elemMatch:{originalWord:{$in:["stacking","tuple"]}}}})
    public Vector<String> findPhraseUrlIntersection(LinkedList<String> OriginalWordsToFind)  //,LinkedList<String> StemmedWordsToFind)
    {

        //Vector<DatabaseComm> originalWordsInfo = new Vector<DatabaseComm>();
        Vector<String> urlIntersect = new Vector<String>();
        BasicDBObject objectToFind = new BasicDBObject();
        objectToFind.put("stemmedWord", new BasicDBObject("$in", OriginalWordsToFind));

        DBCursor wordsFound = collection.find(objectToFind);
        Set<String> setofURLsIntersect;
        Map<String,Vector<DatabaseComm>> UrlBDCommAllWordsMap=new HashMap<>();

        boolean firstVector=true;
        Integer modifiedWordLen = wordsFound.size();  //***not length
        while (modifiedWordLen != 0) {
            Vector<String> urlEachword = new Vector<String>();
//            DatabaseComm toPhraseSearchMap=new DatabaseComm();
            //if (wordsFound.hasNext()) {

            BasicDBObject stemmedWordObj = (BasicDBObject) wordsFound.next();
            BasicDBList wordsList = (BasicDBList) stemmedWordObj.get("words");
            //Filling the map for phrase search
            if (wordsList != null) {
                Iterator<Object> wordsIterator = wordsList.iterator();
                while (wordsIterator.hasNext()) {
                    BasicDBObject wordObj = (BasicDBObject) wordsIterator.next();   //Fatema
                    DatabaseComm originalWordStructure = new DatabaseComm(Integer.parseInt(wordObj.get("tf").toString()),
                            wordObj.get("tag").toString(),
                            wordObj.get("originalWord").toString(),
                            (List<Integer>) wordObj.get("positions"),
                            wordObj.get("url").toString());

                    if (OriginalWordsToFind.contains(wordObj.get("originalWord").toString())) {
                        //originalWordsInfo.add(originalWordStructure);
                        String currentUrl= wordObj.get("url").toString();
                        urlEachword.add(currentUrl);
                        if(UrlBDCommAllWordsMap.containsKey(currentUrl))
                        {
                            boolean we_can_add=true;
                            for (DatabaseComm wordsInfo : UrlBDCommAllWordsMap.get(currentUrl)){
                                // kda el vector bta3 el map el so8ayara unique mn 7eth el orig word wel url
                                if((originalWordStructure.theWord.equals(wordsInfo.theWord)) ) //originalWordStructure.url.equals(currentUrl) &&
                                {
                                    we_can_add = false;
                                }
                            }
                            if(we_can_add)
                                UrlBDCommAllWordsMap.get(currentUrl).add(originalWordStructure);

                        }
                        else {
                            Vector<DatabaseComm> firstElement = new Vector<>();

                            firstElement.add(originalWordStructure);
                            UrlBDCommAllWordsMap.put(wordObj.get("url").toString(), firstElement);
                        }
                    }

                }



            }

            if (firstVector) {
                firstVector = false;
                urlIntersect.addAll(urlEachword);
            } else {
                if (urlEachword.size() != 0) //e7tyaty bs mafrood eno dayman hla2y fe link at least feh el original word
                    urlIntersect.retainAll(urlEachword);
            }


            // }
            --modifiedWordLen;
        }
        setofURLsIntersect=new HashSet<>(urlIntersect);
        System.out.println("URL Intersect length --> "+setofURLsIntersect.size());
        System.out.println(setofURLsIntersect);
        getIntersectionPhraseWordsInfo(setofURLsIntersect,UrlBDCommAllWordsMap,OriginalWordsToFind);
        return urlIntersect;
    }
    // el function d kol hadfha enha trga3ly el vector of DataBaseComm feeh el URLs el intersect bs el moshkela bs
    // btrga3o 5altabita belnesba ll stemwords
    void getIntersectionPhraseWordsInfo(Set<String> intersectedURLS,Map<String,Vector<DatabaseComm> > wordsInfoObjects,LinkedList<String> originalWordsToFind){
        // ana kda b3dy 3ala el set bs ele hya intersection mn kol l urls l kter ele ragen mn l DB
        for (String url:intersectedURLS){
            // hwa akeed el  intersect mwgoood check malosh lazma
            if(wordsInfoObjects.containsKey(url)){
                for (DatabaseComm wordsInfoOnURL : wordsInfoObjects.get(url)){
                     //intersectionWordsInfo.addAll(wordsInfoObjects.get(url));
                        String stemmedWordToMap = stemmerObject.stemWord(wordsInfoOnURL.theWord);
                        // DA HANShEEEEEEELO lama nsala7 el stemmer**********
                        stemmedWordToMap = wordsInfoOnURL.theWord;
                        if (phraseSearchResultFromDB.containsKey(stemmedWordToMap)) {


                            phraseSearchResultFromDB.get(stemmedWordToMap).getRight().add(wordsInfoOnURL);

                            }

                        else{
                            Vector<DatabaseComm> firstElement = new Vector<>();
                            firstElement.add(wordsInfoOnURL);
                            Pair<Integer,Vector<DatabaseComm>> firstElementPair=new Pair<>(originalWordsToFind.indexOf(stemmedWordToMap),firstElement);
                            phraseSearchResultFromDB.put(stemmedWordToMap,firstElementPair);
                        }


                    }
            }


                }


        return;
    }
}