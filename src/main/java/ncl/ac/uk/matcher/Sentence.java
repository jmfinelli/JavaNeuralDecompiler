package ncl.ac.uk.matcher;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface Sentence {

    Set<String> dictionary = new HashSet<>();

    String getClassName();

    String getMethodName();

    String getSentence();

    Map<String, String> getReferences();

    int getTokensNumber();

    static Set<String> getDictionary(){
        return dictionary;
    }
}
