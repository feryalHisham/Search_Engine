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

    public static stopwords checkStopWord;
    public static boolean isRecrawling;




    public String modifyWord(String word,String tag){

        Stemmer porterStemmer = new Stemmer();
        word=word.toLowerCase();
        //Check if Stop word
         if(tag=="p"||tag=="span"||tag=="pre"||tag=="li")
            if(checkStopWord.ifStopWords(word))
               return null ;

        if(checkStopWord.ifCitation(word))
            return null ;
            //Check if special character

         word=word.replaceAll("[^a-zA-Z0-9]", "");

        //else that: Stem the word
        porterStemmer.add(word);
        porterStemmer.stem();
        return porterStemmer.toString();

    }




    public static void indexing(Document doc,String url,boolean cr_re) throws IOException {

    	isRecrawling=cr_re;
        checkStopWord=new stopwords();
        textTags teTags=new textTags();
        //unique for the check on the whole txt afterwards
        final String[] neededTags={"h1","h2", "h3", "h4", "h5", "h6"};//"p","pre","span","li",
        FileWriter outstream= new FileWriter ("outb2a.txt");
        Map<String,DatabaseComm> objToInsert=new HashMap<String,DatabaseComm>();
        DB db=null;

        try {

            MongoClient mongoClient = new MongoClient("localhost", 27017);
            db = mongoClient.getDB("search_engine5");
            System.out.println("Connected to Database");


        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("Server is ready ");
        DBCollection collection = db.getCollection("wordsIndex");
       // collection.createIndex();

        /*
         *
         * Recrawling
         * */


        if (isRecrawling==true){

            BasicDBObject i1= new BasicDBObject();
            BasicDBObject i2= new BasicDBObject();
            BasicDBObject i3= new BasicDBObject();
            BasicDBObject i4= new BasicDBObject();
            BasicDBObject i5= new BasicDBObject();


            i1.put("url",url);
            i2.put("$elemMatch",i1);
            i3.put("urls",i2);
            i4.put("idf",-1);
            i5.put("$inc",i4);
          
            collection.updateMulti(i3,i5);

            BasicDBObject q1 = new BasicDBObject();
            BasicDBObject q2 = new BasicDBObject();
            BasicDBObject q3 = new BasicDBObject();
            q2.put("url",url);
            q3.put("urls",q2);

       
            DBObject update = new BasicDBObject();
            update.put("$pull", q3);
            System.out.println(update);
            collection.updateMulti(i3,update);
    		

        }


        /**********************************************************************************/


        String innerBody=doc.select("body").text();
        /////////////////////////////////////////////////
        for (String tag:neededTags) {
            outstream.write("USED TAG: "+tag+'\n');
            for (Element element : doc.select(tag)) {

                String[] words = element.text().split(" ");

                for (String word : words) {

                    word=teTags.modifyWord(word,tag);
                    if(word==null)
                        continue;
                    if(word.length()==1&&word!="a")
                        continue;


                    if (! objToInsert.containsKey(word)){
                        objToInsert.put(word,new DatabaseComm());
                        objToInsert.get(word).changeTag();


                    }


                    outstream.write(word + " ");

                }

            }
        }


        String[] words = innerBody.split(" ");
        for (int i = 0; i < words.length; i++) {
            String word=words[i];
            if (objToInsert.containsKey(word))
                continue;

            word=teTags.modifyWord(word,"p");
            if(word==null)
                continue;
            if(word.length()==1&&word!="a")
                continue;

            outstream.write(word + " ");
            if (! objToInsert.containsKey(word))
                objToInsert.put(word,new DatabaseComm());
            
            //positions of the word in inner body
            objToInsert.get(word).incOccurence();
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
                urlObject.put("positions",insert.getValue().getPositions());

                BasicDBObject tempisa = new BasicDBObject();
                tempisa.put("$addToSet", new BasicDBObject().append("urls", urlObject));
                collection.update(new BasicDBObject().append("word",insert.getKey()),tempisa);

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
                urlObject.put("tf", insert.getValue().getOccurence());
                urlObject.put("tag",insert.getValue().getTag());
                urlObject.put("positions",insert.getValue().getPositions());

                URLs.add(urlObject);
                theWord.put("urls", URLs);

                collection.insert(theWord);
            }

        }


        outstream.close();

    }
}