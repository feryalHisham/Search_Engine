

import com.mongodb.*;
import com.mongodb.util.JSON;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;


public class test {


    public static void main (String[] args)throws Exception {

        DB db=null;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        try {


            MongoClient mongoClient = new MongoClient("localhost", 27017);
            db = mongoClient.getDB("textStemming");
            System.out.println("Connected to Database");



        } catch (Exception e) {
            System.out.println(e);
        }


       System.out.println("Server is ready ");


        DBCollection collection = db.getCollection("collection");
        BasicDBObject document = new BasicDBObject();
        document.put("hosting", "hostA");
        document.put("type", "vps");
        document.put("clients", 1000);
        collection.insert(document);

        System.out.println("after insertion");
        DBCursor dbCursor = collection.find();
        while(dbCursor.hasNext()){
            System.out.println(dbCursor.next());
        }



    }




}

