import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;
import com.trigonic.jrobotx.RobotExclusion;

import java.util.*;

public class DatabaseComm{

    int occurence;
    String tag;
    String theWord;
    Set<Integer> positions;
    Map<String, Integer > wordtags;
    String url;

//    final static String[] neededTags={"p","pre","span","li","h1","h2", "h3", "h4", "h5", "h6"};


    public void addPosition(int pos) {

        positions.add(pos);
        ++occurence;
    }


    public Set<Integer> getPositions() {
        return positions;
    }

    public DatabaseComm(){
//        wordtags=new HashMap<String,Integer >();
        positions=new HashSet<>();
        occurence=0;
        tag=new String();
        tag="p";

    }

    public DatabaseComm (int occurence,String tag,String theWord,Set<Integer> positions,String url)
    {
        this.occurence =occurence;
        this.tag = tag;
        this.theWord = theWord;
        this.positions = positions;
        this.url=url;
    }

    public int getOccurence() {
        return occurence;
    }

    public Map<String, Integer> getWordtags() {
        return wordtags;
    }

    public void insertWord(String tag){

        occurence++;
        if(wordtags.containsKey(tag)){
            wordtags.put(tag,wordtags.get(tag)+1);
        }
        else
            wordtags.put(tag,1);

    }
    public List<BasicDBObject> getTagOccurrences(){

        List<BasicDBObject> occurrence = new ArrayList<>();
        for (Map.Entry<String, Integer> tagsOccur : wordtags.entrySet()) {

            BasicDBObject occurenceTag = new BasicDBObject();
            occurenceTag.put("tagName", tagsOccur.getKey());
            occurenceTag.put("numOccur", tagsOccur.getValue());
            occurrence.add(occurenceTag);

        }
        return  occurrence;

    }
    public String getTag(){

        return tag;

    }
    public String changeTag(){

        return tag="h";

    }
    public String getUrl()
    {
        return url;
    }
}