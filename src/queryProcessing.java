import com.mongodb.*;

import java.util.*;

public class queryProcessing {
    List<String> words;
    static stopORstem stemmingObj = new stopORstem();
    static dbInterface findInDB;
    public Map<String,Pair< Integer,Vector<DatabaseComm> >> wordsToRanker;

    public queryProcessing(String searchwords){    //,String DBname, String DBCollection){

        words = Arrays.asList(searchwords.split(" "));
        if(words.get(0).equals("\"")  && words.get(words.size()-1).equals("\""))
        {
            doPhraseSearch(words);
        }
        List<String> ModifiedWordsList = removeStopWords(words);
        if (ModifiedWordsList.size()!=0){
            words = ModifiedWordsList;
        }
        wordsToRanker=new HashMap<>();
        findInDB= new dbInterface("search_engine6","WordsIndex");        //(DBname,DBCollection);


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

    public List<String> removeStopWords(List<String> words){

        List <String> modifiedSearchWords = new ArrayList<>();
        String tag = "p";

        for(String word: words){
            String modifiedWord = stemmingObj.modifyWord(word,tag);
            if(modifiedWord!= null)
            { modifiedSearchWords.add(modifiedWord);
            }


        }



        return modifiedSearchWords;
    }
    public void doPhraseSearch(List<String> words){

        /*intersect query to get the documents that contains all the words
        * get the word with min occurrence in each doc to start searching dor the rest of the phrase
        */

        findInDB.findPhraseUrlIntersection(words);

    }

}