package ncl.ac.uk.matcher;

public class BytecodeSentence implements Sentence {

    private final String _sentence;
    private final String _class;
    private final String _method;

    public BytecodeSentence(String className, String methodName, String sentence){
        this._class = className;
        this._method = methodName;
        this._sentence = sentence;
    }

    @Override
    public String getSentence() { return this._sentence; }

    @Override
    public String getClassName() { return this._class; }

    @Override
    public String getMethodName() { return this._method; }
}
