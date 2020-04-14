package ncl.ac.uk.matcher;

import java.io.IOException;
import java.util.List;

public interface JARSReader {

    List<String> getLibrariesNames();

    ClassJavaPair getClassJavaPair(String libraryName) throws IOException;

}
