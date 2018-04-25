import java.util.Map;
import java.util.Vector;

public class testRankerWithQuery {

    static queryProcessing processingQuery;

    public static void main(String[] args) {


        String searchString= "generalization mathematical ";
        processingQuery=new queryProcessing(searchString);
        processingQuery.retreiveSearchWordsInfo();

        System.out.println("Ranker map --->   "+processingQuery.wordsToRanker.size());

        for (Map.Entry<String,Pair<Integer,Vector<DatabaseComm>>> wordsInfoMapEntry: processingQuery.wordsToRanker.entrySet() ) {

            System.out.println("word position --->   "+wordsInfoMapEntry.getValue().getLeft());

            System.out.println("word vector --->   "+wordsInfoMapEntry.getValue().getRight().size());

            //System.out.println("search query "+wordsInfoMapEntry.getKey());
                /*for (  DatabaseComm  wordInfo :wordsInfoMapEntry.getValue()){
                    System.out.println("info:");
                    System.out.println("Original Word: "+wordInfo.getTheWord());
                    System.out.println("URL: "+wordInfo.getUrl());
                    System.out.println("TF "+wordInfo.getOccurence());
                    System.out.println("Tag: "+wordInfo.getTag());
                }*/

        }

    }
    }
