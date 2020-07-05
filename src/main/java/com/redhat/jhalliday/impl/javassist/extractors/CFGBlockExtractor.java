package com.redhat.jhalliday.impl.javassist.extractors;

import com.redhat.jhalliday.TriFunction;
import com.redhat.jhalliday.impl.LineNumber;
import javassist.CtMethod;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.analysis.ControlFlow.Block;
import javassist.bytecode.analysis.ControlFlow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class CFGBlockExtractor implements BiFunction<CtMethod, Map<String, String>, List<Map<LineNumber, String>>> {

    private final TriFunction<CtMethod, Block, Map<String, String>, Map<LineNumber, String>> BlockBodyExtractor;

    public CFGBlockExtractor(TriFunction<CtMethod, Block, Map<String, String>, Map<LineNumber, String>> blockBodyExtractor) {
        BlockBodyExtractor = blockBodyExtractor;
    }

    @Override
    public List<Map<LineNumber, String>> apply(CtMethod ctMethod, Map<String, String> placeholders) {

        ControlFlow CFGs = null;
        try {
            CFGs = new ControlFlow(ctMethod);
        } catch (BadBytecode ex) {
            // impossible
        }

        List<Map<LineNumber, String>> results = new ArrayList<>();

        for (ControlFlow.Block block : CFGs.basicBlocks()) {

            Map<LineNumber, String> blockBody = BlockBodyExtractor.apply(ctMethod, block, placeholders);
            results.add(blockBody);

        }

        return results;
    }
}
