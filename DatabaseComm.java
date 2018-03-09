
import com.mongodb.DB;
import com.mongodb.MongoClient;

import java.util.*;

public class DatabaseComm {

//    public String url;
    public int occurence;
    public Map<String, Integer > wordtags;



    public DatabaseComm(){
        wordtags=new HashMap<String,Integer >();
        occurence=0;
    }

    public int getOccurence() {
        return occurence;
    }

    public Map<String, Integer> getWordtags() {
        return wordtags;
    }

    public void insertWord(String tag){

            occurence++;
            if(wordtags.containsKey(tag))
                wordtags.put(tag,wordtags.get(tag)+1);

    }
    public void initWord(String tag){

        occurence=0;
        if(wordtags.containsKey(tag))
            wordtags.put(tag,0);

    }

}
