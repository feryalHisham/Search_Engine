import com.mongodb.*;
import org.jsoup.nodes.Document;


import java.util.*;

public class queryProcessing {
    LinkedList<String> words;
    static stopORstem stemmingObj = new stopORstem();
    static dbInterface findInDB;
    public Map<String,Pair< Integer,Vector<DatabaseComm> >> wordsToRanker;
    public Map<String,Pair< Integer,Vector<DatabaseComm> >> phraseWordsToRanker;
    public Map<String,Pair< Integer,Vector<DatabaseComm> >> phraseFinalToRanker = new HashMap<>();



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
    public HashMap<String ,DatabaseComm> doPhraseSearch(LinkedList<String> words){

        /*intersect query to get the documents that contains all the words
         * get the word with min occurrence in each doc to start searching for the rest of the phrase
         */
        LinkedList<String> stemmedWords = new LinkedList<>();
        for(String word: words){
            stemmedWords.add(stemmingObj.stemWord(word));
        }
        HashMap<String ,DatabaseComm> urlsIntersectWithLeastOccWord = findInDB.findPhraseUrlIntersection(words); //,stemmedWords); // mafrood asln hasearch 3la el stemmed fel DB

        System.out.println("URL with Words length  " + urlsIntersectWithLeastOccWord.size());


        return urlsIntersectWithLeastOccWord;

    }

    public void preparePhrase(){
        //search query words may conatian more than 2 double quotes
        int firstQuoteidx = words.indexOf("\"");
        int lastQuoteidx = words.lastIndexOf("\"");
        LinkedList<String> wordsBetweenQuotes = new LinkedList<>(words.subList(firstQuoteidx+1,lastQuoteidx)); //first index is inclusive second is exclusive
        HashMap<String ,DatabaseComm> urlsIntersectWithLeastOccWord = doPhraseSearch(wordsBetweenQuotes);
        phraseWordsToRanker=findInDB.phraseSearchResultFromDB;
        getAllPhraseInfo (wordsBetweenQuotes,urlsIntersectWithLeastOccWord);

        // remove from words el phrase kolha klmat w quotes
        words.subList(firstQuoteidx,lastQuoteidx+1).clear();
        //words.removeAll(Collections.singleton("\'"));
        for(String word:wordsBetweenQuotes){
            System.out.println("URL Vector in Map length --> "+phraseWordsToRanker.get(word).getRight().size());
        }

        return;
    }


    public void  getAllPhraseInfo(LinkedList<String> searchPhrase,Map <String,DatabaseComm> docsWordsMap){
        Integer phrasePosInQuery=0;
        String originalSearchPhrase="";//=searchPhrase.toString();        //might not work asln
        for (String queryWord:searchPhrase){
            originalSearchPhrase+=(queryWord+" ");
        }
        Vector<DatabaseComm> dbVectorToFinalMap=new Vector<>();
        for (Map.Entry<String,DatabaseComm> urlDoc : docsWordsMap.entrySet()){
            Pair<Integer,Integer> firstPosAndTF=getEachURLPhraseInfo(searchPhrase,urlDoc);
            DatabaseComm dbCommToFinalMap=new DatabaseComm(firstPosAndTF.getRight(),"p",
                    urlDoc.getValue().theWord,null,urlDoc.getKey());
            dbVectorToFinalMap.add(dbCommToFinalMap);
        }
        Pair<Integer,Vector<DatabaseComm>> urlInfoToFinalMap=new Pair<>(phrasePosInQuery,dbVectorToFinalMap);
        phraseFinalToRanker.put(originalSearchPhrase,urlInfoToFinalMap);
    }



    Pair<Integer,Integer> checkForPhrase(List<String> urlDocWords,LinkedList<String> searchPhrase,Set<Integer> positions,int posInPhrase){
        boolean firstOcc=true,phraseMatched=true;
        Integer firstPosition=0,newOcc=0;
        for (Integer pos:positions){
            for (int i = 1; i <=posInPhrase ; i++) {
                if(!urlDocWords.get(pos-i).equals(searchPhrase.get(posInPhrase-i)))
                {
                    phraseMatched=false;
                    break;
                }
            }
            for (int i = 1; posInPhrase+i<searchPhrase.size() && phraseMatched; i++) {  //kan fe <=
                if(!urlDocWords.get(pos+i).equals(searchPhrase.get(posInPhrase+i))){
                    phraseMatched=false;
                    break;
                }
            }
            if(phraseMatched)
                newOcc++;
            if(firstOcc){
                firstOcc=false;
                firstPosition=pos;
            }
            phraseMatched=true;
        }

        return new Pair<Integer,Integer>(firstPosition,newOcc);
    }
    public Pair<Integer,Integer>  getEachURLPhraseInfo(LinkedList<String> searchPhrase,Map.Entry<String,DatabaseComm> urlDoc){

        int posInPhrase=searchPhrase.indexOf(urlDoc.getValue().theWord);
        Document urlDocHTML=findInDB.getDocByURLCrawlerDB(urlDoc.getKey());
        List<String> urlDocWords=Arrays.asList(urlDocHTML.select("body").text().split(" "));
        return checkForPhrase(urlDocWords,searchPhrase,urlDoc.getValue().positions,posInPhrase);

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