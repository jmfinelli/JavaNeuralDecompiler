package com.redhat.jhalliday.impl.javassist.extractors;

import com.redhat.jhalliday.TriFunction;
import com.redhat.jhalliday.impl.CFGBlockWrapper;
import com.redhat.jhalliday.impl.javassist.CFGBlock;
import javassist.CtMethod;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.analysis.ControlFlow.Block;
import javassist.bytecode.analysis.ControlFlow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class CFGBlockExtractor implements BiFunction<CtMethod, Map<String, String>, List<CFGBlockWrapper>> {

    private final TriFunction<CtMethod, Block, Map<String, String>, String> BlockBodyExtractor;

    public CFGBlockExtractor(TriFunction<CtMethod, Block, Map<String, String>, String> blockBodyExtractor) {
        BlockBodyExtractor = blockBodyExtractor;
    }

    @Override
    public List<CFGBlockWrapper> apply(CtMethod ctMethod, Map<String, String> placeholders) {

        ControlFlow CFGs = null;
        try {
            CFGs = new ControlFlow(ctMethod);
        } catch (BadBytecode ex) {
            // impossible
        }

        List<CFGBlockWrapper> results = new ArrayList<>();

        for (ControlFlow.Block block : CFGs.basicBlocks()) {

            String blockBody = BlockBodyExtractor.apply(ctMethod, block, placeholders);
            results.add(new CFGBlock(block, blockBody));

        }

        return results;
    }
}
