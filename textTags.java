
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class textTags {

    public static void main(String[] args) throws IOException {
        String file="test2.html"; //get from url
        stopwords checkStopWord=new stopwords();
        final String[] textTags={"p","pre","span"};

        BufferedReader reader = new BufferedReader(new FileReader (file));
        String line,getIt="";

        FileWriter outstream= new FileWriter ("outb2a.txt");


        try {
            while((line = reader.readLine()) != null) {
                getIt+=line;            //line.replaceAll("\\[|\\]", "");;
                }
        } finally {
            reader.close();
        }
        final String html= getIt;
        ///////////////////////////////////////////
        Document doc = Jsoup.parse(html);
        /////////////////////////////////////////////////
        for (String tag:textTags) {
            outstream.write("USED TAG: "+tag+'\n');
            for (Element element : doc.select(tag)) {

                String[] words = element.text().split(" ");
                Stemmer porterStemmer = new Stemmer();
//                String dummyWord="";
                for (String word : words) {


                    //Check if special character
                    //word=word.replaceAll("\\[|+^\\d+\\]", "");
                    word=word.replaceAll("[^a-zA-Z0-9]", "");

                    word=word.toLowerCase();
//                    dummyWord=word;
                    //Check if Stop word
                    if(checkStopWord.ifStopWords(word))
                        continue;
                    //else that: Stem the word
                    porterStemmer.add(word);
                    porterStemmer.stem();


                    outstream.write(porterStemmer.toString() + " ");

                }

            }
        }

        outstream.close();
    }
}
