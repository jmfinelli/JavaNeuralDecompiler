package ncl.ac.uk.matcher;

public interface DecompilationRecord {

    Object getLowLevelBytecode();

    Object convertLowLevel();

    Object getHighLevelSourceCode();

    Object convertHighLevel();
}
