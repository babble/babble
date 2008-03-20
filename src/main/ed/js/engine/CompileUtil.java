// CompileUtil.java

package ed.js.engine;

import java.io.*;
import java.net.*;
import java.util.*;

import ed.io.*;
import ed.util.*;

class CompileUtil {
    
    static boolean D = Convert.D;
    static boolean CD = false;
    
    static final String TMP_DIR = "/tmp/jxp/";// + Math.random() + "/";

    static synchronized Class compile( String p , String c , String source )
        throws IOException , ClassNotFoundException {
        
        if ( CD ) System.err.println( "compile called" );
        if ( D ) System.out.println( source );
        if ( CD ) System.err.println( "compile 0 " );        

        File dir = new File( TMP_DIR + p.replace( '.' , '/' ) + "/" );
        dir.mkdirs();

        if ( CD ) System.err.println( "compile 1 " );

        File f = new File( dir , c + ".java" );
        File output = new File( f.getAbsolutePath().replaceAll( "java$" , "class" ) );
        long depend = getDependencyLastTime();

        if ( CD ) System.err.println( "compile 2 " );

        String old = null;
        if ( f.exists() )
            old = StreamUtil.readFully( new FileInputStream( f ) );

        boolean oldOK = source.equals( old ) && output.exists() && output.lastModified() > depend;

        if ( CD ) System.err.println( "compile 3 " );

        if ( ! oldOK ){
        
            FileOutputStream fout = new FileOutputStream( f );
            fout.write( source.getBytes() );
            fout.close();

            if ( CD ) System.err.println( "compile 3.1 " );
            
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter( sw ); 
            
            if ( CD ) System.err.println( "compile 3.2 " );
            if ( CD ) System.err.println( "compile 3.2.1 " );
            int res = com.sun.tools.javac.Main.compile( new String[]{ "-g" , f.toString() } , pw );
            if ( D ) System.out.println( f + " : " + res );

            if ( CD ) System.err.println( "compile 3.3 " );
        
            if ( res != 0 ){
                System.err.println( "**" + sw );
                throw new RuntimeException( sw.toString() );
            }

            if ( CD ) System.err.println( "compile 3.4 " );

        }

        if ( CD ) System.err.println( "compile 4 " );
            
        URLClassLoader cl = new URLClassLoader( new URL[]{ (new File( TMP_DIR )).toURL() } );
        return cl.loadClass( p + "." + c );
    }

    static long getDependencyLastTime(){
        if ( _dependFiles.size() == 0 )
            return Long.MAX_VALUE;
        
        long max = 0;
        for ( File f : _dependFiles ){
            max = Math.max( max , f.lastModified() );
        }
        return max;
    }
    
    private static Set<File> _dependFiles = new HashSet<File>();
    private static String _dependsDirs[] = new String[]{ "src/main/ed/js" , 
                                                 "src/main/ed/js/engine" , 
                                                 "src/main/ed/js/func" };

    static {
        for ( String dirName : _dependsDirs ){
            File dir = new File( dirName );

            if ( ! ( dir.exists() && dir.isDirectory() ) ){
                System.out.println( "bad dir : " + dir );
                _dependFiles.clear();
                break;
            }

            for ( File f : dir.listFiles() ){
                if ( ! f.getName().endsWith( ".java" ) )
                    _dependFiles.add( f );
            }
        }
    }
    
    
}
