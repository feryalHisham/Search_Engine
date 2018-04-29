
import com.mongodb.*;
import org.bson.types.ObjectId;

import java.util.*;



import static java.lang.Double.max;
import static java.lang.Double.min;

public class Relevance_Ranker {



    Map<String, Pair<Integer, Vector<DatabaseComm>>> Query_words;  //integer is the position of word in query
    Vector<Integer> idfs;
    Map<String, Vector<Double>> tfs;
    Map<String, Vector<Set<Integer>>> pos;
    Map<String,Pair<Pair<Double,Double>,Integer>> pos_score;   //pair<min_diff,no_of_query_words appear in it>   //starting snippet position
    Map<String, Integer> starting_snippet_of_urls_with_no_QWs;
    Double max_url_pos=0.0;
    Pair<Double,Double> the_min_diff_of_url_with_max_QWs;


    HashMap<String,Pair<Double,Pair<Integer,Boolean>>> final_rank;

    Map<String, Double> ranks_from_popularity;


    MongoClient mongoClient;
    DB database;

    Relevance_Ranker(Map<String, Pair<Integer, Vector<DatabaseComm>>> temp) {

        Query_words = new HashMap<>();

        mongoClient = new MongoClient();
        database = mongoClient.getDB("search_engine10");


        Query_words = temp;

        //arr is for initialization only
        Integer [] arr = new Integer[Query_words.size()];
        idfs = new Vector<Integer>(Arrays.asList(arr));
        Collections.fill(idfs, 0);


        pos_score=new HashMap<>();

        tfs = new HashMap<>();
        pos = new HashMap<>();
        final_rank = new HashMap<>();

        the_min_diff_of_url_with_max_QWs=new Pair<>(0.0,0.0);
        starting_snippet_of_urls_with_no_QWs=new HashMap<>();

        ranks_from_popularity=new HashMap<>();


    }

    public Integer fill_vector_with_positions(Vector<Set<Integer>> positions_of_QWs)
    {
        SortedSet<Integer> set_of_all_positions= new  TreeSet<>();

        for(int i=0;i<positions_of_QWs.size();i++)
        {
            if(!positions_of_QWs.get(i).isEmpty())
            set_of_all_positions.addAll(positions_of_QWs.get(i));
        }

        Vector<Integer> vector_of_all_positions=new Vector<>(set_of_all_positions);

        //call the function to get the best position to take snippet

        return vector_of_all_positions.elementAt(getStartSnippingPosition(vector_of_all_positions,50)); ////////????????????????
    }

    public Pair<Pair<Double,Double>,Integer> Calc_min_diff_during_pos_calc(Vector<Set<Integer>> positions_of_QWs)
    {

        Double sum_of_mins=0.0;

        Double [] arr_double = new Double[Query_words.size()]; //for initialization
        //it's a temporary vector to be used while calculating
        Vector<Double> min_diff = new Vector<Double>(Arrays.asList(arr_double));
        Collections.fill(min_diff, Double.POSITIVE_INFINITY);


        Double no_of_words_in_this_url=0.0;
        for (int first = 0; first < positions_of_QWs.size(); first++) {

            //check that set is not empty to make sure this url doesn't contain this query word
            if(!positions_of_QWs.elementAt(first).isEmpty())
            {
                no_of_words_in_this_url++;

                for (int second = first+1; second < positions_of_QWs.size(); second++) {

                    if(!positions_of_QWs.elementAt(second).isEmpty()){

                        Vector FrstL = new Vector(positions_of_QWs.elementAt(first));
                        Vector SecL = new Vector(positions_of_QWs.elementAt(second));
                        double result;

                        if (FrstL.size()<SecL.size())
                           result = get_min_diff(FrstL,SecL);
                        else result = get_min_diff(SecL,FrstL);

                        min_diff.set(first, Math.min(min_diff.get(first),result));
                        min_diff.set(second, Math.min(min_diff.get(second),result));
                    }
                }

            }


            //lma aalaaeeh fady ana kda al min diff almfrod a7to bl max mslan fhyfdl b 0 wnfhm an da allmfrod yba b3d kda 1

        }


        for(int i=0;i<min_diff.size();i++)
        {
            if(min_diff.elementAt(i)!=Double.POSITIVE_INFINITY)
                sum_of_mins+=min_diff.elementAt(i);
        }



        Integer starting_snippet=fill_vector_with_positions(positions_of_QWs); //to calculate best position to take snippet


        return new Pair(new Pair<>(sum_of_mins,no_of_words_in_this_url),starting_snippet);
    }

    public void Calc_pos()
    {

        for (String url : pos.keySet()) {
            Vector<Set<Integer>> positions_of_QWs = pos.get(url);

            Pair<Pair<Double,Double>,Integer> sum_of_min_diff__no_of_words=Calc_min_diff_during_pos_calc( positions_of_QWs); //al integer da starting snippet
            max_url_pos=max(max_url_pos,sum_of_min_diff__no_of_words.getLeft().getLeft());
            if(the_min_diff_of_url_with_max_QWs.getLeft()<sum_of_min_diff__no_of_words.getLeft().getRight())
            {
                the_min_diff_of_url_with_max_QWs.setLeft(sum_of_min_diff__no_of_words.getLeft().getRight());
                the_min_diff_of_url_with_max_QWs.setRight(sum_of_min_diff__no_of_words.getLeft().getLeft());
            }
            pos_score.put(url,sum_of_min_diff__no_of_words); //we will devide the max_url_pos by these values while calculating

            //pos score don't contain all urls(urls which is not here will take 1)

        }


    }


    public void Calc_IR_Score_pos() {

        Vector<Double> tfs_of_QW;

        for (String url : tfs.keySet()) {

            tfs_of_QW=tfs.get(url);

            Double IR_score = 0.0;
            for (int i = 0; i < tfs_of_QW.size(); i++) {

                //calculate IR_score of this url
               IR_score+=tfs_of_QW.elementAt(i)*idfs.elementAt(i);

            }

            Double IR_score_pos = 0.0;
            Integer starting_snippet;

            //dah ma3nah an mfesh wla url feh aktr mn klma mn al QWs fh3del benhom fl7sba wa5leeh 1
            if(the_min_diff_of_url_with_max_QWs.getRight()==0.0&&the_min_diff_of_url_with_max_QWs.getLeft()==1)
            {
                the_min_diff_of_url_with_max_QWs.setRight(1.0);
                max_url_pos=1.0;  //hma asln klohom asfar
            }

            if(pos_score.containsKey(url))
            {

                Double no_of_infinities=Query_words.size()-pos_score.get(url).getLeft().getRight();
                IR_score_pos=IR_score*(max_url_pos/(pos_score.get(url).getLeft().getLeft()+(the_min_diff_of_url_with_max_QWs.getRight()*no_of_infinities)))
                *(pos_score.get(url).getLeft().getRight()/Query_words.size());

                starting_snippet=pos_score.get(url).getRight();
            }
            else
            {
                //al url da mknsh fe klma zay al query_word fa al pos hn3tbro b 1/no_of_query_words aswaa 7aga
                IR_score_pos=IR_score*(max_url_pos/((the_min_diff_of_url_with_max_QWs.getRight()*Query_words.size())))*(1.0/Query_words.size());

                //wl snippet bta30 hyt7dd mn al map altania

                starting_snippet=starting_snippet_of_urls_with_no_QWs.get(url);

            }


           Pair<Double,Boolean> rank_from_popularity_ranker= get_rank_from_db(url);; //to be changed  //boolean is_video
            //code will be added here



            final_rank.put(url, new Pair(IR_score_pos*rank_from_popularity_ranker.getLeft(),new Pair(starting_snippet,rank_from_popularity_ranker.getRight())));
        }
    }


    public void Calc_IDFs(Integer query_word_pos, String query_word, DatabaseComm original_data) {
        if (original_data.theWord.equals(query_word)) {
            idfs.set(query_word_pos, idfs.elementAt(query_word_pos) + 1);
        }
    }

    public void Calc_TFs(Integer query_word_pos, String query_word, DatabaseComm original_data) {
        String url = original_data.getUrl();

        double H_P = 0;

        if (original_data.getTag().equals("P"))
            H_P = 0.75;
        else
            H_P = 1;
        Double tf_to_add = original_data.getOccurence() * (1 / (Diff_String(query_word, original_data.theWord+1))) * H_P;
        if (tfs.containsKey(url)) {
            tfs.get(url).set(query_word_pos, tfs.get(url).elementAt(query_word_pos) + tf_to_add);
        } else {


            //arr is for initialization only
            Double [] arr = new Double[Query_words.size()];
            Vector<Double> for_initiallize = new Vector<Double>(Arrays.asList(arr));
            Collections.fill(for_initiallize, 0.0);

            tfs.put(url, for_initiallize );

           // for_initiallize.clear();  //5las msh 3izah


            tfs.get(url).set(query_word_pos, tfs.get(url).elementAt(query_word_pos) + tf_to_add);
        }


    }

    public void Fill_pos(Integer query_word_pos, String query_word, DatabaseComm original_data) {
        String url = original_data.getUrl();

        if (query_word.equals(original_data.theWord)) {


            if (pos.containsKey(url)) {
                pos.get(url).set(query_word_pos, original_data.getPositions());
            } else {

                if( starting_snippet_of_urls_with_no_QWs.containsKey(url))
                {
                    //check if exist in this map erase it
                    starting_snippet_of_urls_with_no_QWs.remove(url);
                }
                //this for initiallization only
                Vector<Set<Integer>> for_initiallize = new Vector<Set<Integer>>();
                for(int i=0;i<Query_words.size();i++)
                {
                 for_initiallize.add(s1); //s1 is an empty set fro iniiallization
                }
                //-----------------------

                pos.put(url,for_initiallize);


                pos.get(url).set(query_word_pos, original_data.getPositions());

            }
        }

        else
        {
            //this url doesn't contain QW but we need to set the position of snippet and it may be erased if a QW being found soon
            Iterator iter = original_data.getPositions().iterator();

            starting_snippet_of_urls_with_no_QWs.put(url, (Integer) iter.next());
            //msh fara m3aya hya50d position anhy klma fe alw7shen fmsh m7taga atcheck hna hwa kan mawgod wla la
        }


    }

    public void Calc_TFs_IDFs_Fill_pos() {


        for (String query_word : Query_words.keySet()) {
            Integer query_word_pos = Query_words.get(query_word).getLeft();
            Vector<DatabaseComm> originals = Query_words.get(query_word).getRight();

            for (int i = 0; i < originals.size(); i++) {
                DatabaseComm original_data = originals.elementAt(i);
                //calculate the idfs
                Calc_IDFs(query_word_pos, query_word, original_data);

                //calculate the tfs
                Calc_TFs(query_word_pos, query_word, original_data);

                //fill the pos
                Fill_pos(query_word_pos, query_word, original_data);


            }
        }


    }

    public  Double Diff_String( String Q, String P){

        Vector<String> BFS_Diff_Q=new Vector<String>();
        Vector<String> BFS_Diff_P=new Vector<>();
        Vector<Double> BFS_Diff_val=new Vector<>();

        int index =0 ;
        BFS_Diff_Q.add(Q);
        BFS_Diff_P.add(P);

        BFS_Diff_val.add(0.0);

        return get_diff_num(index,BFS_Diff_Q,BFS_Diff_P,BFS_Diff_val);


    }

    public  Double get_diff_num(int index,Vector<String> BFS_Diff_Q,Vector<String> BFS_Diff_P,Vector<Double> BFS_Diff_val){



        String Q = BFS_Diff_Q.get(index);
        String P =BFS_Diff_P.get(index);

        int Diff_position = difference (Q,P);
        if (Q==null){
            if (P==null)
                return BFS_Diff_val.get(index);
            else {
                return BFS_Diff_val.get(index) + P.length();
            }

        }
        else if (P==null){
            if (Q==null)
                return BFS_Diff_val.get(index);
            else {
                return BFS_Diff_val.get(index) + Q.length();
            }


        }
        else if (Diff_position==-1){

            return BFS_Diff_val.get(index);
        }
        else {


            String emptyS="";
            String Q_shifted;
            String P_shifted;
            if (Q.length()==Diff_position){
                Q_shifted = emptyS;
            }
            else {
                Q_shifted= Q.substring(Diff_position+1);
            }
            if (P.length()==Diff_position){
                P_shifted=emptyS;
            }
            else {
                P_shifted= P.substring(Diff_position+1);
            }

            Q= Q.substring(Diff_position);
            P= P.substring(Diff_position);
            // first child set q & p+1

            BFS_Diff_Q.add(Q);
            BFS_Diff_P.add(P_shifted);
            BFS_Diff_val.add(BFS_Diff_val.get(index)+1);


            //second child set q+1 & p

            BFS_Diff_Q.add(Q_shifted);
            BFS_Diff_P.add(P);
            BFS_Diff_val.add(BFS_Diff_val.get(index)+1);

            //third child set q+1 & p+1

            BFS_Diff_Q.add(Q_shifted);
            BFS_Diff_P.add(P_shifted);
            BFS_Diff_val.add(BFS_Diff_val.get(index)+1);


            index = index+1;
            return get_diff_num(index,BFS_Diff_Q,BFS_Diff_P, BFS_Diff_val);


        }

    }


    public  int difference(String str1, String str2) {
        if (str1 == null) {
            return -1;
        }
        if (str2 == null) {
            return -1;
        }
        int at = indexOfDifference(str1, str2);
        if (at == -1) {
            return -1;
        }
        return  at;
    }

    public  int indexOfDifference(CharSequence cs1, CharSequence cs2) {
        if (cs1 == cs2) {
            return -1;
        }
        if (cs1 == null || cs2 == null) {
            return -1;
        }
        int i;
        for (i = 0; i < cs1.length() && i < cs2.length(); ++i) {
            if (cs1.charAt(i) != cs2.charAt(i)) {
                break;
            }
        }
        if (i < cs2.length() || i < cs1.length()) {
            return i;
        }
        return -1;
    }


    public  double get_min_diff(Vector<Integer>firstLst,Vector<Integer>secondLst){

        double min =-1;

        for (int i = 0; i < firstLst.size() ; i++) {

            int min_res = minDiffBsearch(secondLst,0,secondLst.size(),firstLst.get(i));
            double temp_min= Math.abs(firstLst.get(i)-min_res);
            if (min==-1)
                min=temp_min;
            else if (temp_min<min){
                min= temp_min;
            }
        }


        return min;


    }

    public int minDiffBsearch(Vector<Integer> List, int low, int high, int k) {


        int mid = (low + high) / 2;




        if (mid==0){
            if (List.size()>1)
            if (Math.abs(List.get(0)-k) < Math.abs(List.get(1)-k) )
            {
                return List.get(0);
            }
            else {
                return List.get(1);
            }
            else return List.get(0);
        }

        else if (mid == List.size())
            return List.get(high-1);

        if (mid==List.size())
        {

            return List.get(high-1);

        }

        if (low > high) {

            if (Math.abs(List.get(low)-k) < Math.abs(List.get(high)-k) )
            {
                return List.get(low);
            }
            else {
                return List.get(high);
            }

            //return -1

        } else if (List.get(mid )== k) {
            return List.get(mid );
        } else if (List.get(mid ) < k) {
            return minDiffBsearch(List, mid + 1, high, k);
        } else {
            return minDiffBsearch(List, low, mid - 1, k);
        }
    }

    public  int getStartSnippingPosition(Vector<Integer>List,int filterSize){

        int words=0;
        int position=0;
        for (int i = 0; i <List.size()-1 ; i++) {
            int result = getMinPosCompFilterBsearch(List,i+1,i+1,List.size(),List.get(i)+filterSize);
            int diffWord= result - i+1;
            if (result == -1)
                continue;
            if ( diffWord >words){
                words = diffWord;
                position=i;
            }



        }

       // System.out.println(words);
        return position;
    }

    public int getMinPosCompFilterBsearch(Vector<Integer> L,int start, int low, int high, int k) {


        int mid = (low + high) / 2;

        if (mid == start){
            if (L.get(mid)<=k)
                return mid;
            return -1;
        }

        if (mid == L.size()){
            if (L.get(mid-1)<=k)
                return high-1;
            return -1;
        }



        if (low > high) {
            if (L.get(low)>k && L.get(high)>k)
                return -1;
            else if (L.get(high)<=k)
                return high;
            else if (L.get(low)<=k)
                return  low;
            return -1;



        } else if (L.get(mid) == k) {
            return mid;
        } else if (L.get(mid) < k) {
            return getMinPosCompFilterBsearch(L, start,mid + 1, high, k);
        } else {
            return getMinPosCompFilterBsearch(L, start,low, mid - 1, k);
        }
    }

    public Map<String, Pair<Double,Pair<Integer,Boolean>>> get_pages_sorted_from_ranker()
    {

        Calc_TFs_IDFs_Fill_pos();
        Calc_pos();

        Calc_IR_Score_pos();

        System.out.println(final_rank.size());
        Map<String,Pair<Double,Pair<Integer,Boolean>>> sorted_final_map = sortByValue(final_rank); //this is a map sorted on values decriesingly
        System.out.println(sorted_final_map.size());
        return sorted_final_map;
    }
    public void Calc_IR_Score_phrase(){

        //al for loop de httnfz mra wa7da laan hya klma wa7da
        for(String Phrase_word: Query_words.keySet()) {

            Vector<DatabaseComm> originals = Query_words.get(Phrase_word).getRight();
            Integer IDF=originals.size();
            for (int i = 0; i <originals.size();i++)
            {

                Integer IR_score = originals.get(i).getOccurence()*IDF;
                Pair<Double,Boolean> rank_from_popularity_ranker = get_rank_from_db(originals.get(i).url);; //to be changed
                //code will be added here



                Iterator iter = originals.get(i).getPositions().iterator();
                System.out.println("iterator phrase : "+originals.get(i).getPositions());

                Object first = iter.next();
                System.out.println("posistion phrase : "+first);
                final_rank.put(originals.get(i).getUrl(), new Pair(IR_score * rank_from_popularity_ranker.getLeft()
                        , new Pair(first,rank_from_popularity_ranker.getRight())));

            }
        }


    }
    public  Pair<Double,Boolean> get_rank_from_db(String url)
    {
        Double rank=1.0;
        Boolean is_video=null;
        DBCollection collection = database.getCollection("url");

        BasicDBObject fieldObject = new BasicDBObject();
        fieldObject.put("pr", 1);
        fieldObject.put("is_video", 1);

        DBCursor cursor = collection.find(new BasicDBObject("url_name", url),
                fieldObject);


        while (cursor.hasNext()) {
            // System.out.println("only one parent at a time for "+
            // key);
            BasicDBObject object = (BasicDBObject) cursor.next();
            rank = object.getDouble("pr");
            is_video=object.getBoolean("is_video");


        }

        return new Pair<>(rank,is_video);
    }
    public Map<String, Pair<Double,Pair<Integer,Boolean>>> get_pages_sorted_from_ranker_phrase()
    {


        Calc_IR_Score_phrase();

        Map<String, Pair<Double,Pair<Integer,Boolean>>> sorted_final_map = sortByValue(final_rank); //this is a map sorted on values decriesingly

        return sorted_final_map;
    }

   /* public static Map sortByValue(Map unsortedMap) {
        Map sortedMap = new TreeMap(new ValueComparator(unsortedMap));
        sortedMap.putAll(unsortedMap);
        return sortedMap;
    }*/


    private static Map<String, Pair<Double,Pair<Integer,Boolean>>> sortByValue(Map<String, Pair<Double,Pair<Integer,Boolean>>> unsortMap) {

        // 1. Convert Map to List of Map
        List<Map.Entry<String, Pair<Double,Pair<Integer,Boolean>>>> list =
                new LinkedList<Map.Entry<String, Pair<Double,Pair<Integer,Boolean>>>>(unsortMap.entrySet());

        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order
        Collections.sort(list, new Comparator<Map.Entry<String, Pair<Double,Pair<Integer,Boolean>>>>() {
            public int compare(Map.Entry<String, Pair<Double,Pair<Integer,Boolean>>> o1,
                               Map.Entry<String, Pair<Double,Pair<Integer,Boolean>>> o2) {
                return (o2.getValue().getLeft()).compareTo(o1.getValue().getLeft());
            }
        });

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        Map<String, Pair<Double,Pair<Integer,Boolean>>> sortedMap = new LinkedHashMap<String, Pair<Double,Pair<Integer,Boolean>>>();
        for (Map.Entry<String, Pair<Double,Pair<Integer,Boolean>>> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }



        return sortedMap;
    }
    //for test
    public static void main(String[] args) throws Exception{


        //to be removed
        queryProcessing processingQuery;
        //to be removed
        String searchString= "moments deutsch";

        long startTime = System.nanoTime();

        processingQuery=new queryProcessing(searchString);
        processingQuery.retreiveSearchWordsInfo();

        /*

       SortedSet<Integer> s= new  TreeSet<>();


        Map<String, Pair<Integer, Vector<DatabaseComm>>> temp=new HashMap<>();


        Vector<DatabaseComm> v=new Vector<>();
        DatabaseComm d=new DatabaseComm();
        d.theWord="engineer";
        d.occurence=3;
        d.tag="P";
        d.url="youtube";
        s.add(3);
        s.add(4);
        s.add(10);
        d.positions=s;
        v.add(d);

        d=new DatabaseComm();
        d.theWord="engineering";
        d.occurence=7;
        d.tag="H";
        d.url="youtube";
        s= new TreeSet<>();
        s.add(5);
        s.add(7);
        s.add(20);
        d.positions=s;
        v.add(d);


        d=new DatabaseComm();
        d.theWord="engines";
        d.occurence=7;
        d.tag="H";
        d.url="tutorialspoint";
        s= new TreeSet<>();
        s.add(4);
        s.add(8);
        s.add(30);
        d.positions=s;
        v.add(d);


        temp.put("engineering",new Pair<>(0,v));

        v=new Vector<>();

        d=new DatabaseComm();
        d.theWord="beautes";
        d.occurence=3;
        d.tag="P";
        d.url="facebook";
        s= new TreeSet<>();
        s.add(3);
        s.add(4);
        s.add(10);
        d.positions=s;
        v.add(d);

        d=new DatabaseComm();
        d.theWord="beauty";
        d.occurence=5;
        d.tag="H";
        d.url="youtube";
        s= new TreeSet<>();
        s.add(6);
        s.add(10);
        s.add(18);
        d.positions=s;
        v.add(d);

        d=new DatabaseComm();
        d.theWord="beautiful";
        d.occurence=7;
        d.tag="H";
        d.url="tutorialspoint";
        s= new TreeSet<>();
        s.add(5);
        s.add(7);
        s.add(20);
        d.positions=s;
        v.add(d);


        temp.put("beautiful",new Pair<>(1,v));**/

        Relevance_Ranker re=new Relevance_Ranker(processingQuery.wordsToRanker);
        Map<String,Pair<Double,Pair<Integer,Boolean>>> map=re.get_pages_sorted_from_ranker();

        System.out.print("total time of relevance rank: ");
        System.out.println(System.nanoTime()-startTime);
//        Map<String,Pair<Double,Pair<Integer,Boolean>>> map=new HashMap<>();
//
//        map.put("heba",new Pair(4.0,new Pair<>(2,false)));
//        map.put("feryal",new Pair(10.0,new Pair<>(2,false)));
//        map.put("fatema",new Pair(3.0,new Pair<>(2,false)));
//
//        Map<String,Pair<Double,Pair<Integer,Boolean>>> map_s=sortByValue(map);



       /* Map<String, Pair<Integer, Vector<DatabaseComm>>> temp2=new HashMap<>();


        v=new Vector<>();
        d=new DatabaseComm();
        d.theWord="i am heba";
        d.occurence=3;
        d.tag="P";
        d.url="youtube";
        s.add(3);
        s.add(4);
        s.add(10);
        d.positions=s;
        v.add(d);

        d=new DatabaseComm();
        d.theWord="i am heba";
        d.occurence=5;
        d.tag="H";
        d.url="stackoverflow";
        s= new TreeSet<>();
        s.add(5);
        s.add(7);
        s.add(20);
        d.positions=s;
        v.add(d);


        d=new DatabaseComm();
        d.theWord="i am heba";
        d.occurence=7;
        d.tag="H";
        d.url="tutorialspoint";
        s= new TreeSet<>();
        s.add(4);
        s.add(8);
        s.add(30);
        d.positions=s;
        v.add(d);


        temp2.put("i am heba",new Pair<>(0,v));

        Relevance_Ranker re2=new Relevance_Ranker(temp2);
        Map<String,Pair<Double,Integer>> map2=re2.get_pages_sorted_from_ranker_phrase();

*/

        // s.clear();
        System.out.println("h");


    }




    //this set for initiallization purpose only
    SortedSet<Integer> s1= new  TreeSet<>();

}







    /*



*/