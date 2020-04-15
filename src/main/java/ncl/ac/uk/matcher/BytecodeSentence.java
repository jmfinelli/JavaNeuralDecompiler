package ncl.ac.uk.matcher;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BytecodeSentence implements Sentence {

    private final String _sentence;
    private final String _class;
    private final String _method;
    private int _tokens = 0;
    private final Map<String, String> _constantPool = new HashMap<>();

    public BytecodeSentence(String className, String methodName, String sentence){
        this._class = className;
        this._method = methodName;
        this._sentence = this.sentencePostProcessing(sentence);
    }

    @Override
    public String getSentence() { return this._sentence; }

    @Override
    public String getClassName() { return this._class; }

    @Override
    public String getMethodName() { return this._method; }

    @Override
    public Map<String, String> getReferences() { return this._constantPool; }

    @Override
    public int getTokensNumber() { return this._tokens; }

    /**
     * This private method post-processes the String passed as parameter
     * @param sentence Sentence to post process
     * @return post processed String
     */
    private String sentencePostProcessing(String sentence) {

        Pattern pattern = Pattern.compile("(#\\d+)(\\s=\\s\\w+\\s)([^\\s]+)");
        Pattern costantPattern = Pattern.compile("(#\\d+)(\\s=\\s)\"(.*)\"");
        Pattern tokenPattern = Pattern.compile("(\\w+.+\\n)");

        String sentenceWithoutLineNumber = sentence.replaceAll("\\d+:\\s", "");
        Matcher matcher = pattern.matcher(sentenceWithoutLineNumber);
        Matcher costantMatcher = costantPattern.matcher(sentenceWithoutLineNumber);

        while (matcher.find())
            this._constantPool.put(matcher.group(1), matcher.group(3));
        while (costantMatcher.find())
            this._constantPool.put(costantMatcher.group(1), costantMatcher.group(3));

        String cleanedSentence = sentenceWithoutLineNumber
                .replaceAll(pattern.pattern(), "$1")
                .replaceAll(costantPattern.pattern(), "$1");

        Matcher tokenMatcher = tokenPattern.matcher(cleanedSentence);

        while(tokenMatcher.find()) {
            Sentence.dictionary.add(tokenMatcher.group(1));
            this._tokens++;
        }

        return cleanedSentence;
    }
}
