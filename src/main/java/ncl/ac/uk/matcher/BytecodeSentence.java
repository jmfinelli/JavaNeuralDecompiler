package ncl.ac.uk.matcher;

import java.util.Map;

public class BytecodeSentence implements Sentence {

    private final String _sentence;
    private final String _class;
    private final String _method;
    private final int _tokens;
    private final Map<String, String> _references;

    public BytecodeSentence(String className, String methodName, String sentence, Map<String, String> references, int tokens){
        this._class = className;
        this._method = methodName;
        this._sentence = sentence;
        this._references = references;
        this._tokens = tokens;
    }

    @Override
    public String getSentence() { return this._sentence; }

    @Override
    public String getClassName() { return this._class; }

    @Override
    public String getMethodName() { return this._method; }

    @Override
    public Map<String, String> getReferences() { return this._references; }

    @Override
    public int getTokensNumber() { return this._tokens; }
}
