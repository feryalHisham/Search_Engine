import org.tartarus.snowball.ext.englishStemmer;

public class stopORstem {
    stopwords checkStopWord;
    stopORstem(){

        checkStopWord=new stopwords();
    }

    public String modifyWord(String word,String tag){


//        word=word.toLowerCase();
        prepareWord(word);
        //Check if Stop word
        if(tag.equals("p")||tag.equals("span")||tag.equals("pre")||tag.equals("li"))
            if(checkStopWord.ifStopWords(word))
                return null;

        if(checkStopWord.ifCitation(word))
            return null;

        //Check if special character
//        word=word.replaceAll("[^a-zA-Z0-9]", "");
        if (word.equals(""))
            return null;
        //else that: Stem the word
//        porterStemmer.add(word);
//        porterStemmer.stem();
        /////////////////////////////////******E7NA SHAYLEEEEEEN EL STEMMMER*********/////////////////////////////
 //       System.out.println(stemWordSnowball(word));
        return stemWordSnowball(word);

    }
    public String prepareWord(String word){
        word=word.toLowerCase();
        word=word.replaceAll("[^a-zA-Z0-9]", "");
        return word;
    }

    public String stemWordPorter(String word){
        Stemmer porterStemmer = new Stemmer();
        prepareWord(word);
        porterStemmer.add(word);
        porterStemmer.stem();
        return porterStemmer.toString();
    }

    public String stemWordSnowball(String word){
        englishStemmer stemmer = new englishStemmer();
        stemmer.setCurrent(word);
        stemmer.stem();
        return stemmer.getCurrent();
    }
}
