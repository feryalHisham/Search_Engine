

import java.util.ArrayList;
import java.util.List;

import com.mongodb.*;



public class test {


    public static void main (String[] args)throws Exception {

        DB db=null;
        try {


            MongoClient mongoClient = new MongoClient("localhost", 27017);
            db = mongoClient.getDB("search_engine4");
            System.out.println("Connected to Database");



        } catch (Exception e) {
            System.out.println(e);
        }


       System.out.println("Server is ready ");
       
       



        DBCollection collection = db.getCollection("wordsIndex");
      
            /*BasicDBObject theWord = new BasicDBObject();
            theWord.put("word","heba");
            
            theWord.put("idf", 2);



            List<BasicDBObject> URLs = new ArrayList<>();
            BasicDBObject urlObject = new BasicDBObject();
            urlObject.put("url","https://www.geeksforgeeks.org/");
            urlObject.put("tf", 1);
            urlObject.put("tag","p");
            List <Integer> positions=new ArrayList<Integer>();
            positions.add(1);
            positions.add(1);
            positions.add(1);
            urlObject.put("positions",positions);

            URLs.add(urlObject);
//            urlObject.put("positions", insert.getValue().getPositions());
            
            urlObject = new BasicDBObject();
            urlObject.put("url","https://www.youtube.org/");
            urlObject.put("tf", 1);
            urlObject.put("tag","p");
           
            urlObject.put("positions",positions);

            URLs.add(urlObject);
            
            theWord.put("urls", URLs);

            collection.insert(theWord);
            
            
            theWord = new BasicDBObject();
            theWord.put("word","feryal");
            
            theWord.put("idf", 2);



           URLs = new ArrayList<>();
            urlObject = new BasicDBObject();
            urlObject.put("url","https://www.geeksforgeeks.org/");
            urlObject.put("tf", 1);
            urlObject.put("tag","p");
           
            urlObject.put("positions",positions);

            URLs.add(urlObject);
//            urlObject.put("positions", insert.getValue().getPositions());
            
            urlObject = new BasicDBObject();
            urlObject.put("url","https://www.tutorials.org/");
            urlObject.put("tf", 1);
            urlObject.put("tag","p");
            
            urlObject.put("positions",positions);

            URLs.add(urlObject);
            
            theWord.put("urls", URLs);

            collection.insert(theWord);
*/

        String SURL="https://www.geeksforgeeks.org/";
        BasicDBObject i1= new BasicDBObject();
        BasicDBObject i2= new BasicDBObject();
        BasicDBObject i3= new BasicDBObject();
        BasicDBObject i4= new BasicDBObject();
        BasicDBObject i5= new BasicDBObject();


        i1.put("url",SURL);
        i2.put("$elemMatch",i1);
        i3.put("urls",i2);
        i4.put("idf",-1);
        i5.put("$inc",i4);
      
        collection.updateMulti(i3,i5);

        BasicDBObject q1 = new BasicDBObject();
        BasicDBObject q2 = new BasicDBObject();
        BasicDBObject q3 = new BasicDBObject();
        q2.put("url",SURL);
        q3.put("urls",q2);

   
        DBObject update = new BasicDBObject();
        update.put("$pull", q3);
        System.out.println(update);
        collection.updateMulti(i3,update);
		
         System.out.println("5alst ya bashr...");
         
        /*DBCursor result = collection.find(q3);
        while (result.hasNext()) {
            BasicDBObject querry = (BasicDBObject) result.next();//new BasicDBObject("url",SURL);
            DBObject update_idf = new BasicDBObject();
            update_idf.put("$inc", new BasicDBObject("idf", -1));
            collection.updateMulti(querry, update_idf);

        }*/
      /*  BasicDBObject b1 = new BasicDBObject();
        BasicDBObject b2 = new BasicDBObject();
        BasicDBObject b3 = new BasicDBObject();
        BasicDBObject b4 = new BasicDBObject();
        BasicDBObject b5 = new BasicDBObject();

        b2.put("url",SURL );
        b3.put("urls",b2);
        b4.put("$pull",b3);
        b5.put("multi","true");


        collection.update(b1,b4,false,true);



//        DBCursor dbCursor = collection.find(theWord);
//        if (dbCursor.hasNext()){
//
//        }
//        BasicDBObject idfinc = new  BasicDBObject().append("$inc",
//                new BasicDBObject().append("idf", 1));
//
//        collection.update(new BasicDBObject().append("word","german"),idfinc);
//
//
//        List<BasicDBObject> occurence = new ArrayList<>();
//
//
//            BasicDBObject occurenceTag = new BasicDBObject();
//            occurenceTag.put("tagName", "h1");
//            occurenceTag.put("numOccur", 16);
//            occurence.add(occurenceTag);
//
//        BasicDBObject occurenceTag2 = new BasicDBObject();
//        occurenceTag2.put("tagName", "pre");
//        occurenceTag2.put("numOccur", 29);
//        occurence.add(occurenceTag2);
//
//

//        BasicDBObject urlObject = new BasicDBObject();
//        urlObject.put("url", "https://bos_b2a_3l4an_tb2a_3arf");
//
//        BasicDBObject tempisa = new BasicDBObject();
//        tempisa.put("$push", new BasicDBObject().append("urls", urlObject));
//        collection.update(new BasicDBObject().append("word","shallow"),tempisa);

*/
    }




}

