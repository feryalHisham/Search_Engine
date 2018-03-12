

import com.mongodb.*;



public class test {


    public static void main (String[] args)throws Exception {

        DB db=null;
        try {


            MongoClient mongoClient = new MongoClient("localhost", 27017);
            db = mongoClient.getDB("search_engine");
            System.out.println("Connected to Database");



        } catch (Exception e) {
            System.out.println(e);
        }


       System.out.println("Server is ready ");


        BasicDBObject theWord = new BasicDBObject();

        theWord.put("word","german");


        DBCollection collection = db.getCollection("wordsIndex");
/*
*
* nm7 kol ele y7os l url
* */
        String SURL="https://www.geeksforgeeks.org/";
        BasicDBObject q1 = new BasicDBObject();
        BasicDBObject q2 = new BasicDBObject();
        BasicDBObject q3 = new BasicDBObject();

        q1.put("url",SURL);
        q2.put("",q1);
        q3.put("urls",q2);

        DBObject update_idf = new BasicDBObject();
        update_idf.put("$inc", new BasicDBObject("idf", -1));
        collection.updateMulti(q3, update_idf);
        
        DBObject update = new BasicDBObject();
		update.put("$pull", q3);
		collection.updateMulti(q3, update);
		
		
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

