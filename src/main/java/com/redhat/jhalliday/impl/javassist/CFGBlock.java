package com.redhat.jhalliday.impl.javassist;

import com.redhat.jhalliday.impl.CFGBlockWrapper;
import javassist.bytecode.analysis.ControlFlow.Block;

import java.util.ArrayList;
import java.util.List;

public class CFGBlock extends CFGBlockWrapper<Block> {

    public CFGBlock(Block basicBlock, String blockBody) {
        this.basicBlock = basicBlock;
        this.firstInstrPosition = basicBlock.position();
        this.length = basicBlock.length();
        this.index = basicBlock.index();
        this.blockBody = blockBody;
    }
}
