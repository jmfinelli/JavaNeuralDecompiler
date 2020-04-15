package ncl.ac.uk.transformation.impl;

import ncl.ac.uk.transformation.TransformerFunction;

import java.io.File;
import java.util.Map;

public class JarContentRecordTransformer extends CompositeRecordTransformer<File, File, Map<String, byte[]>, Map<String, byte[]>> {

    public JarContentRecordTransformer(
            TransformerFunction<File, Map<String, byte[]>> lowTransform,
            TransformerFunction<File, Map<String, byte[]>> highTransform) {
        super(lowTransform, highTransform);
    }
}
