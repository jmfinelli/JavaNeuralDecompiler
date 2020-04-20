package ncl.ac.uk.matcher;

import java.util.List;

public interface ClassJavaPair {

    String getSrcPath();

    String getBinPath();

    String getSourceJARFile();

    List<String> getDotJavaFiles();

    List<DecompilationRecord> getDecompilationRecords(String dotJavaFile) throws Exception;

    List<BytecodeRepresentation> getBytecodeRepresentations(String dotJavaFile) throws Exception;

    List<ASTRepresentation> getASTRepresentations(String dotJavaFile) throws Exception;

    List<String> getMethodsNames(String dotJavaFile);
}
