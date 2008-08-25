// WorkingFiles.java

package ed.io;

import java.io.*;

public class WorkingFiles {
    
    public static final String TMP_DIR = "/tmp/jxp/";
    public static final File TMP_FILE = new File( TMP_DIR );
    static {
        TMP_FILE.mkdirs();
    }

    public static File getTypeDir( String type ){
        File f = new File( TMP_FILE , type );
        f.mkdirs();
        return f;
    }

    public static File getTMPFile( String type , String name ){
        name = FileUtil.clean( name );

        while ( name.startsWith( "/" ) )
            name = name.substring(1);
        
        File f = new File( getTypeDir( type ) , name );
        
        if ( name.contains( "/" ) )
            f.getParentFile().mkdirs();

        return f;
    }
    
}
