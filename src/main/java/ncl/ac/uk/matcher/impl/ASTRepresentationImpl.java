package ncl.ac.uk.matcher.impl;

import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import ncl.ac.uk.matcher.ASTRepresentation;
import ncl.ac.uk.matcher.BytecodeRepresentation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ASTRepresentationImpl implements ASTRepresentation {

    private final String _sentence;
    private final String _class;
    private final MethodInfo _methodInfo;
    private int _tokensNumber = 0;
    private final Map<String, String> _references = new HashMap<>();

    public ASTRepresentationImpl(String className, MethodInfo info, String sentence){
        this._class = className;
        this._methodInfo = info;
        this._sentence = sentence;
        this.sentencePostProcessing(sentence);
    }

    @Override
    public String getRepresentation() { return this._sentence; }

    @Override
    public String getClassName() { return this._class; }

    @Override
    public String getMethodName() { return this._methodInfo.getName(); }

    @Override
    public Map<String, String> getReferences() { return this._references; }

    @Override
    public int getTokensNumber() { return this._tokensNumber; }

    @Override
    public ConstPool getClassPool() { return this._methodInfo.getConstPool(); }

    /**
     * Extract tokens from the parameter sentence
     * @param sentence Sentence to post process
     */
    private void sentencePostProcessing(String sentence) {

        List<String> tokens = Arrays.asList(sentence.split("\\s+"));

        this._tokensNumber =+ tokens.size();

        BytecodeRepresentation.dictionary.addAll(tokens);
    }
}
