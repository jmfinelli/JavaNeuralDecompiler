package ncl.ac.uk.matcher;

import javassist.bytecode.ConstPool;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface BytecodeSentence {

    Set<String> dictionary = new HashSet<>();

    String getClassName();

    String getMethodName();

    String getSentence();

    ConstPool getClassPool();

    Map<String, String> getReferences();

    int getTokensNumber();

    static Set<String> getDictionary(){
        return dictionary;
    }
}
