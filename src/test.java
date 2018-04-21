

import com.mongodb.*;
import com.mongodb.util.JSON;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class test {



    private MongoException e;
    static DB db=null;

    test(){
        try {


            MongoClient mongoClient = new MongoClient("localhost", 27017);
            db = mongoClient.getDB("bl3b");
            System.out.println("Connected to Database");



        } catch (Exception e) {
            System.out.println(e);
        }


        System.out.println("Server is ready ");

    }

    static String findStemmedWord(String errormsg)
    {

            Matcher m = Pattern.compile("[\"]([^\"]*)[\"]").matcher(errormsg);  //("[0-9a-f]{24}")

            m.find();
//            System.out.println(m.group());
            return m.group().replace("\"","");



    }
    static BasicDBObject dummycreate(String name,int tf){
        BasicDBObject toInsert = new BasicDBObject();

        toInsert.put("stemmedWord", name);
        toInsert.put("idf", 1);

        List<Integer> pos=new ArrayList<>();
        pos.add(4);
        pos.add(5);
        pos.add(8);
        List<BasicDBObject> words = new ArrayList<>();
        BasicDBObject wordObject = new BasicDBObject();
        wordObject.put("url", "www.youtube.com");
        wordObject.put("originalWord", "be");
        wordObject.put("tf", tf);
        wordObject.put("tag", "p");

        wordObject.put("positions", pos);
        words.add(wordObject);
        toInsert.put("words", words);

        return toInsert;

    }




    public static void main (String[] args)throws Exception {

        try {


            MongoClient mongoClient = new MongoClient("localhost", 27017);
            db = mongoClient.getDB("bl3b");
            System.out.println("Connected to Database");



        } catch (Exception e) {
            System.out.println(e);
        }


        System.out.println("Server is ready ");
        DBCollection collection = db.getCollection("wordsIndex");
        BasicDBObject index= new BasicDBObject("stemmedWord",1);
        collection.createIndex(index,null,true);

//        collection.createIndex("stemmedWord");


        LinkedList<BasicDBObject> listToInsert = new LinkedList<>();

        listToInsert.add( dummycreate("lk",1));
        listToInsert.add( dummycreate("Zh2t",5));
        listToInsert.add( dummycreate("APT",5));
        listToInsert.add( dummycreate("ANA",9));
        listToInsert.add( dummycreate("kj",9));
        listToInsert.add( dummycreate("ANA",5));
        listToInsert.add( dummycreate("kksfj",1));
        listToInsert.add( dummycreate("heh",4));
        listToInsert.add( dummycreate("lol",8));
        listToInsert.add( dummycreate("ANA",5));
        listToInsert.add( dummycreate("kksfj",1));

//        collection.insert(toInsert);

        try{
        collection.insert(listToInsert);
        } catch(MongoException e){
            System.out.println(e.getMessage());
            if (e.getCode() == 11000){
                String erroredStemmedWord=findStemmedWord(e.getMessage());
                int r= listToInsert.size();
                boolean firstTime=false;

                for (int i = 0; i < r; ++i)  {
//                    System.out.println(reInsert.get("stemmedWord"));
                    System.out.println("indexloop "+i);
                    if(listToInsert.getFirst().get("stemmedWord").equals(erroredStemmedWord))
                    {
                        if(!firstTime)
                        {
                            firstTime=true;
                        }
                        else {
                            //TODO: CAll update for the held object and insert to the rest of the list

                            //b7oto fe array b2a
                            BasicDBObject tempisa = new BasicDBObject();

                            String temptemp = listToInsert.getFirst().get("words").toString();
                            String s1 = temptemp.substring(1,temptemp.length()-1);
                            System.out.println(s1);
                            BasicDBObject dbObject =  (BasicDBObject)JSON.parse(s1);


                            tempisa.put("$addToSet", new BasicDBObject().append("words",dbObject)); //mmkn tb2a $push

                            collection.update(new BasicDBObject().append("stemmedWord", erroredStemmedWord),tempisa);
//
                            listToInsert.removeFirst();
                            collection.insert(listToInsert);
                            break;
                        }

                    }
                    System.out.println(listToInsert.getFirst().get("stemmedWord"));

                    listToInsert.removeFirst();    // ftema 5awafaaaaaaaaaaaaa
                }

            }
        }
    }




}

