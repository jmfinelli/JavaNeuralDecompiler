package ncl.ac.uk.matcher;

import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;

import java.util.*;

public class BytecodeSentenceImpl implements BytecodeSentence {

    private final String _sentence;
    private final String _class;
    private final String _method;
    private final ConstPool _constPool;
    private int _tokensNumber = 0;
    private final Map<String, String> _constantPool = new HashMap<>();

    public BytecodeSentenceImpl(String className, MethodInfo info, String sentence){
        this._class = className;
        this._method = info.getName();
        this._constPool = info.getConstPool();
        this._sentence = sentence;
        this.sentencePostProcessing(sentence);
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
    public int getTokensNumber() { return this._tokensNumber; }

    @Override
    public ConstPool getClassPool() { return this._constPool; }

    /**
     * Extract tokens from the parameter sentence
     * @param sentence Sentence to post process
     */
    private void sentencePostProcessing(String sentence) {

        List<String> tokens = Arrays.asList(sentence.split("\\s+"));

        this._tokensNumber =+ tokens.size();

        BytecodeSentence.dictionary.addAll(tokens);
    }
}
