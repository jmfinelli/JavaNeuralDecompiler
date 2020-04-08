package ncl.ac.uk.matcher;

import java.util.List;
import java.util.Map;

public interface ClassJavaInterface {

    String getSrcPath();

    String getBinPath();

    String getDotJavaFile();

    Map<String, String> getMethodBytecodes(String methodName);

    Map<String, String> getMethodsBytecodes();

    List<String> getMethodsNames();
}
