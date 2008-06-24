// FileUtil.java

package ed.io;

import java.io.*;

public class FileUtil {
    public static void deleteDirectory( File f ){

        if ( f.isDirectory() ){
            for ( File c : f.listFiles() )
                deleteDirectory( c );
        }
        
        f.delete();
    }
}
