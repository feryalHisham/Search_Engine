import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

public class Did_you_mean {

    public static String did_you_mean(String query) throws IOException {
        String true_query="";

        String QWs[]=query.split(" ");
        for(int i=0;i<QWs.length;i++)
        {
            if(!QWs[i].equals(""))
            {
                String true_word=get_did_you_mean(QWs[i]);
                true_query+=true_word;
                if(i<QWs.length-1)
                    true_query+=" ";
            }

        }

        return true_query;

    }
    public static String get_did_you_mean(String QW) throws IOException {
       String result="";
        File dir = new File("/home/heba/Documents/cmp/third_year/apt/searchEngineTagmee3/");
        Directory directory = FSDirectory.open(dir);

        SpellChecker spellChecker = new SpellChecker(directory);

        spellChecker.indexDictionary(

        new PlainTextDictionary(new File("/home/heba/Documents/cmp/third_year/apt/searchEngineTagmee3/dictionary.txt")));

        String wordForSuggestions =  QW;

        int suggestionsNumber = 5;

        String[] suggestions = spellChecker.

        suggestSimilar(wordForSuggestions, suggestionsNumber);

        if (suggestions!=null && suggestions.length>0) {
            boolean first=true;
            for (String word : suggestions) {

                System.out.println("Did you mean:" + word);
                if(first)
                {
                    result=word;
                    first=false;
                }


            }

        }

	        else {

        System.out.println("No suggestions found for word:"+wordForSuggestions);

    }

     return result;

    }

    //for test
    public static void main(String[] args) throws Exception {


        System.out.println(Did_you_mean.did_you_mean("mathmatical moments"));

    }

    }
