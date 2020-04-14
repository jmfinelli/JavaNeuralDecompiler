package ncl.ac.uk.matcher;

import java.util.Map;

public interface Sentence {

    String getClassName();

    String getMethodName();

    String getSentence();

    Map<String, String> getReferences();

    int getTokensNumber();
}
