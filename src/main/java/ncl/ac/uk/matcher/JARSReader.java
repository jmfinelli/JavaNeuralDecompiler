package ncl.ac.uk.matcher;

import java.io.IOException;
import java.util.List;

public interface JARSReader {

    List<String> getLibraryNames();

    ClassJavaInterface getClassJavaPair(String libraryName) throws IOException;

}
