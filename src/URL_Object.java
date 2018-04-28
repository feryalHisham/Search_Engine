import org.bson.types.ObjectId;

import java.util.Vector;

public class URL_Object {

    ObjectId obj_id;
    //String url_name;
    Double rank;
    Integer out_links_no;
    Vector<ObjectId> parent_links;


    URL_Object(ObjectId url_id,Double pr,Integer o,Vector<ObjectId> p)
    {
       //url_name=url;
        obj_id=url_id;
       rank=pr;
       out_links_no=o;
       parent_links=p;
    }

}
