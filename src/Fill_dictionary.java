import com.mongodb.*;
import org.bson.types.ObjectId;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class Fill_dictionary {


   MongoClient mongoClient;
   DB database;
    DBCollection collection;

    SortedSet<String> dictionary_words= new  TreeSet<>();


    Fill_dictionary() {
        mongoClient = new MongoClient();
        database = mongoClient.getDB("search_engine7");
        collection = database.getCollection("WordsIndex");

    }

    public void print_dictionary()
    {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("dictionary.txt", "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        for(String word:dictionary_words)
        {
            writer.println(word);

        }

        writer.close();


    }

    public void fill_dictionary_for_did_you_mean() {



        // get the id of the parent url by its name and selects only the field
        // url_name to return
        DBCursor cursor = collection.find();

        while (cursor.hasNext()) {

            BasicDBObject object = (BasicDBObject) cursor.next();
            BasicDBList wordsList = (BasicDBList) object.get("words");
            if (wordsList != null) {
                Iterator<Object> wordsIterator = wordsList.iterator();
                while (wordsIterator.hasNext()) {

                    BasicDBObject wordObj = (BasicDBObject) wordsIterator.next();

                    String word=wordObj.get("originalWord").toString().replaceAll("[^a-zA-Z]", "");
                    if(wordObj.get("originalWord").toString().equals(word)&&!word.equals(""))
                        dictionary_words.add(word);

                }

            }


        }

        print_dictionary();
    }

    //for test
    public static void main(String[] args) throws Exception {

        Fill_dictionary f=new Fill_dictionary();
        f.fill_dictionary_for_did_you_mean();
    }

}

