package ncl.ac.uk.matcher;

import com.sun.xml.internal.ws.commons.xmlutil.Converter;

public interface DecompilationRecord {

    Object getLowLevelBytecode();

    Object convertLowLevel();

    Object getHighLevelSourceCode();

    Object convertHighLevel();
}
