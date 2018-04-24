import com.mongodb.*;

import java.util.*;

public class queryProcessing {
    LinkedList<String> words;
    static stopORstem stemmingObj = new stopORstem();
    static dbInterface findInDB;
    public Map<String,Pair< Integer,Vector<DatabaseComm> >> wordsToRanker;

    public queryProcessing(String searchwords){    //,String DBname, String DBCollection){
        //searchwords = " \" stack generalization \" ";
        words = new LinkedList<String>(Arrays.asList(searchwords.split(" ")));
        wordsToRanker=new HashMap<>();
        findInDB= new dbInterface("search_engine6","WordsIndex");        //(DBname,DBCollection);


        LinkedList<String> ModifiedWordsList = removeStopWords(words);
        if (ModifiedWordsList.size()!=0){
            words = ModifiedWordsList;
        }

        if(words.contains("\""))
        {
            //gets the words between quotes and removes the phrase from words
            preparePhrase();

        }





            }

    public Vector<DatabaseComm> retreive_stemmed_word_info(String word){
        System.out.println("Searching for -->"+ word);
        if(wordsToRanker.containsKey(word))
            return wordsToRanker.get(word).getRight() ;
        return findInDB.findByStemmedWord( word); //stemmingObj.stemWord(word));

    }

    public void retreiveSearchWordsInfo(){
        for(String word:words){

            Pair<Integer,Vector<DatabaseComm>> wordPair = new Pair<>(words.indexOf(word),retreive_stemmed_word_info(word));
            wordsToRanker.put(word,wordPair);
        }
    }

    public LinkedList<String> removeStopWords(List<String> words){

        LinkedList <String> modifiedSearchWords = new LinkedList<>();
        String tag = "p";

        for(String word: words){
            String modifiedWord = stemmingObj.modifyWord(word,tag);
            if(modifiedWord!= null)
            { modifiedSearchWords.add(modifiedWord);
            }


        }



        return modifiedSearchWords;
    }
    public void doPhraseSearch(LinkedList<String> words){

        /*intersect query to get the documents that contains all the words
        * get the word with min occurrence in each doc to start searching dor the rest of the phrase
        */
        LinkedList<String> stemmedWords = new LinkedList<>();
        for(String word: words){
            stemmedWords.add(stemmingObj.stemWord(word));
        }
        Vector<String> urlsIntersected = findInDB.findPhraseUrlIntersection(words); //,stemmedWords); // mafrood asln hasearch 3la el stemmed fel DB
        System.out.println("URL Intersect length --> "+urlsIntersected.size());
        System.out.println(urlsIntersected);
        return;

    }

    public void preparePhrase(){

        int firstQuoteidx = words.indexOf("\"");
        int lastQuoteidx = words.lastIndexOf("\"");
        LinkedList<String> wordsBetweenQuotes = new LinkedList<>(words.subList(firstQuoteidx+1,lastQuoteidx)); //first index is inclusive second is exclusive
        doPhraseSearch(wordsBetweenQuotes);

        // remove from words el phrase kolha klmat w quotes
        words.subList(firstQuoteidx,lastQuoteidx+1).clear();
        //words.removeAll(Collections.singleton("\'"));

        return;
    }

}

/* 3ayzeen n3ml el map bta3t el phrase search kol key by3bar 3n kelma mn el phrase (mn 8eer el stop words bardo l2n asln
            m3mlnash search DB 3leeha)
*wana sha8ala eftakart en asln taree2t el ranking fel 3ady 8eer fel phrase w homa asln nb3atlhom bool kda de phrase wala klmat 3adya
* f2olt yb2a el afdal tb2a two maps 34an nb3at kol wa7da mostaqela w e7na lama neegy n show el results n7ot bta3et el phrase el awel
* sa7 wla eh?? :D
* bs gahzelna el map de b2a bta3et el phrase
* 
* */