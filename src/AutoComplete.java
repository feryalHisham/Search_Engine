import com.mongodb.*;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class AutoComplete {

    dbInterface db_autocomp;

    public AutoComplete(){



        db_autocomp = new dbInterface();
        db_autocomp.connectDBCollection("search_engine10","history");

    }


    public String findPatterninDB(String userSearch){



        String [] userSearchSplited = userSearch.split(" ");
        StringBuilder patternBuilder = new StringBuilder();
        for(String userPattern : userSearchSplited) {
            Pattern my_pattern = Pattern.compile(Pattern.quote(userPattern));
            System.out.println(db_autocomp.findHistory(my_pattern));

            patternBuilder.append(db_autocomp.findHistory(my_pattern)).append(" ");
        }

        if(patternBuilder.length()==0) return "no suggestions";

            return patternBuilder.toString();


    }



}


