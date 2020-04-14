package ncl.ac.uk.matcher;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ClassJavaPair {

    String getSrcPath();

    String getBinPath();

    String getSourceJARFile();

    List<String> getDotJavaFiles();

    List<DecompilationRecord> getDecompilationRecords(String dotJavaFile) throws Exception;

    List<Sentence> getSentences(String dotJavaFile) throws Exception;

    List<String> getMethodsNames(String dotJavaFile);
}
