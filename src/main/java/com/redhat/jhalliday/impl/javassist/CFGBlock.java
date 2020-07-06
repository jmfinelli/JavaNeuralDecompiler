package com.redhat.jhalliday.impl.javassist;

import com.redhat.jhalliday.impl.CFGBlockWrapper;
import com.redhat.jhalliday.impl.LineNumber;
import javassist.bytecode.analysis.ControlFlow.Block;

import java.util.Map;

public class CFGBlock extends CFGBlockWrapper<Block> {

    public CFGBlock(Block basicBlock, Map<LineNumber, String> blockBody) {
        this.basicBlock = basicBlock;
        this.firstInstrPosition = basicBlock.position();
        this.length = basicBlock.length();
        this.index = basicBlock.index();
        this.blockBody = blockBody;
    }
}
