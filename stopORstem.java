public class stopORstem {
    stopwords checkStopWord;
    stopORstem(){

        checkStopWord=new stopwords();
    }

    public String modifyWord(String word,String tag){

        Stemmer porterStemmer = new Stemmer();
        word=word.toLowerCase();
        //Check if Stop word
        if(tag=="p"||tag=="span"||tag=="pre"||tag=="li")
            if(checkStopWord.ifStopWords(word))
                return null;

        if(checkStopWord.ifCitation(word))
            return null;

        //Check if special character
        word=word.replaceAll("[^a-zA-Z0-9]", "");

        //else that: Stem the word
        porterStemmer.add(word);
        porterStemmer.stem();
        return porterStemmer.toString();

    }
}
