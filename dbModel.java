import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.Map;

public class dbModel {
    static public Map<String,Map<String,DatabaseComm>> urlStemmedWords;


    static stopORstem modifier;
    final static String[] neededTags={"h1","h2", "h3", "h4", "h5", "h6"};


    dbModel(){
        urlStemmedWords=new HashMap<>();
        modifier=new stopORstem();
    }


    public void addHeaderWords(Document doc){

        for (String tag:neededTags) {
            for (Element element : doc.select(tag)) {

                String[] words = element.text().split(" ");

                for (String word : words) {

                    word=word.toLowerCase();
                    String stemmedWord=modifier.modifyWord(word,tag);
                    //check if word is still the original word
                    if(word==null) //then it is not a stopword
                        continue;
                    //Stemming the non stopwords
                    if(!urlStemmedWords.containsKey(stemmedWord))
                    {
                        Map<String,DatabaseComm> originalWordObject=new HashMap<>();
                        originalWordObject.put(word,new DatabaseComm());
                        urlStemmedWords.put(stemmedWord,originalWordObject);
                    }
//                    urlStemmedWords.get(stemmedWord).get(word).changeTag();  //aman nfse just in case
//                    }
                      else if (! urlStemmedWords.get(stemmedWord).containsKey(word)){
                        urlStemmedWords.get(stemmedWord).put(word,new DatabaseComm());
                    }
                    urlStemmedWords.get(stemmedWord).get(word).changeTag();


                }

            }
        }
    }

    public void addToURLMap(String originalWord, int position){

        //Stemming
        String stemmedWord=modifier.modifyWord(originalWord,"p");
        if(stemmedWord==null)
            return;
        if(urlStemmedWords.containsKey(stemmedWord)) {
            if(urlStemmedWords.get(stemmedWord).containsKey(originalWord)) {
                urlStemmedWords.get(stemmedWord).get(originalWord).addPosition(position);
            }else{
                urlStemmedWords.get(stemmedWord).put(originalWord,new DatabaseComm());
                urlStemmedWords.get(stemmedWord).get(originalWord).addPosition(position);
            }

        }else {
            Map<String,DatabaseComm> originalWordObject=new HashMap<>();
            originalWordObject.put(originalWord,new DatabaseComm());
            urlStemmedWords.put(stemmedWord,originalWordObject);
            urlStemmedWords.get(stemmedWord).get(originalWord).addPosition(position);

        }
    }

    public  Map<String, Map<String, DatabaseComm>> getWordsMap() {
        return urlStemmedWords;
    }

    public void setUrlStemmedWords(Map<String, Map<String, DatabaseComm>> urlStemmedWords) {
        dbModel.urlStemmedWords = urlStemmedWords;

    }
}
