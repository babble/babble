// CompileUtil.java

package ed.js.engine;

import java.io.*;
import java.net.*;

class CompileUtil {
    
    static boolean D = Convert.D;
    
    static final String TMP_DIR = "/tmp/jxp/" + Math.random() + "/";

    // TODO: delete when done

    static Class compile( String p , String c , String source )
        throws IOException , ClassNotFoundException {
        
        if ( D ) System.out.println( source );
        
        File dir = new File( TMP_DIR + p.replace( '.' , '/' ) + "/" );
        dir.mkdirs();

        File f = new File( dir , c + ".java" );
        FileOutputStream fout = new FileOutputStream( f );
        fout.write( source.getBytes() );
        fout.close();

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter( sw ); 
        
        int res = com.sun.tools.javac.Main.compile( new String[]{ f.toString() } , pw );
        
        if ( D ) System.out.println( f + " : " + res );
        
        if ( res != 0 )
            throw new RuntimeException( sw.toString() );
        
        URLClassLoader cl = new URLClassLoader( new URL[]{ (new File( TMP_DIR )).toURL() } );
        return cl.loadClass( p + "." + c );
    }
}
