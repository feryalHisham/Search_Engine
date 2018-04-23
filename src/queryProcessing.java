import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import java.util.*;

public class queryProcessing {
    String[] words;
    DB db = null;
    DBCollection collection;
    static stopORstem stemmingObj = new stopORstem();
    static dbInterface findInDB;
    public Map<String,Vector<DatabaseComm> > wordsToRanker;

    public queryProcessing(String searchwords){    //,String DBname, String DBCollection){

        words= searchwords.split(" ");
        wordsToRanker=new HashMap<>();
        findInDB= new dbInterface("search_engine","wordsIndex");        //(DBname,DBCollection);
            /*try {

                MongoClient mongoClient = new MongoClient("localhost", 27017);
                db = mongoClient.getDB(DBname);
                System.out.println("Connected to Database");

            } catch (Exception e) {
                System.out.println(e);
            }
            System.out.println("Server is ready ");
            collection = db.getCollection(DBCollection);
//            BasicDBObject index= new BasicDBObject("stemmedWord",1);
//            collection.createIndex(index,null,true);
*/

        }

        public Vector<DatabaseComm> retreive_stemmed_word_info(String word){

           return findInDB.findByStemmedWord( stemmingObj.stemWord(word));

        }

        public void retreiveSearchWordsInfo(){
            for(String word:words){
                wordsToRanker.put(word,retreive_stemmed_word_info(word));
            }
        }
    }
