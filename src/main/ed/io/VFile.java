// VFile.java

package ed.io;

import java.io.*;

public abstract class VFile {

    public static VFile create( File f ){
        return new VLocalFile( f );
    }


    //  -------

    protected abstract long realLastModified();
    protected abstract boolean realExists();
    
    public abstract InputStream openInputStream()
        throws IOException;

    public abstract boolean isDirectory();

    // ----
    
    public final long lastModified(){
        // TODO: production cache
        return realLastModified();
    }

    public final boolean exists(){
        // TODO: production cache
        return realExists();
    }
    
}
