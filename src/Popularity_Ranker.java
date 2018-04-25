import com.mongodb.*;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.UpdateOneModel;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;

public class Popularity_Ranker {

    MongoClient mongoClient;
    DB database;
    Map<ObjectId,URL_Object> urls;


    Popularity_Ranker()
    {
        urls=new HashMap<ObjectId, URL_Object>();
        mongoClient = new MongoClient();
        database = mongoClient.getDB("search_engine7");

    }

    public void Rank_urls(int no_of_iterations)
    {
        long startTime = System.nanoTime();
        get_all_links_to_work_on();

        for(int i=0;i<no_of_iterations;i++)
        {
            Run_Rank_Algorithm();
        }

        write_Update_ranks_to_DB();
        System.out.print("total time of populariy rank: ");
        System.out.println(System.nanoTime()-startTime);
    }

    public void get_all_links_to_work_on() {

        DBCollection collection = database.getCollection("url");

        // get the id of the parent url by its name and selects only the field
        // url_name to return
        DBCursor cursor = collection.find();

        while (cursor.hasNext()) {

            BasicDBObject object = (BasicDBObject) cursor.next();
            ObjectId obj_id = (ObjectId) object.get("_id");
            Double rank = 1.0;
            Integer out_links_no=object.getInt("out_links_no");


            BasicDBList parent_ids = (BasicDBList) object.get("in_links_id");
            Vector<ObjectId> parent_links_id = null;
            if (parent_ids != null) {
                //has parents
                Iterator<Object> it = parent_ids.iterator();
                parent_links_id = new Vector<ObjectId>();
                while (it.hasNext()) {
                    ObjectId tid = (ObjectId) it.next();
                    parent_links_id.add(tid);
                }

            }

            URL_Object url_data=new URL_Object(obj_id,rank,out_links_no,parent_links_id);

            urls.put(obj_id,url_data);

        }
    }

    private void write_Update_ranks_to_DB() {

        // TODO Auto-generated method stub
        DBCollection collection = database.getCollection("url");
        BasicDBObject url;

        BulkWriteOperation builder = collection.initializeUnorderedBulkOperation();

        for(ObjectId url_id:urls.keySet())
           {


               builder.find(new BasicDBObject("_id",url_id ))
                       .update(new BasicDBObject("$set", new BasicDBObject("pr", urls.get(url_id).rank)));

           }

        builder.execute();

    }



    public void Run_Rank_Algorithm() {

        for (ObjectId url_id : urls.keySet()) {

            Vector<ObjectId> parent_ids=urls.get(url_id).parent_links;
            Double page_rank=0.0;
            if(parent_ids!=null)
            {
                for(int i=0;i<parent_ids.size();i++)
                {
                    page_rank+=((urls.get(parent_ids.elementAt(i)).rank)/urls.get(parent_ids.elementAt(i)).out_links_no);
                }
            }

            urls.get(url_id).rank=page_rank;

            //urls.put(url_id,urls.get(url_id));


        }


    }

    public static void main(String[] args) throws Exception {

        Popularity_Ranker r=new Popularity_Ranker();
        r.Rank_urls(50);

    }

    }
