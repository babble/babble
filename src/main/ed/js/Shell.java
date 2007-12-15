// Shell.java

package ed.js;

import java.io.*;
import java.util.*;

import jline.*;

import ed.db.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.appserver.*;

public class Shell {
    
    static final PrintStream _originalPrintStream = System.out;

    final static OutputStream _myOutputStream = new OutputStream(){
            
            public void write( byte b[] , int off , int len ){
                RuntimeException re = new RuntimeException();
                re.fillInStackTrace();
                re.printStackTrace();
                _originalPrintStream.write( b , off , len );
            }
            
            public void write( int b ){
                _originalPrintStream.write( b );
                throw new RuntimeException("sad" );
            }
        };

    public final static PrintStream _myPrintStream = new PrintStream( _myOutputStream );

    public static void addNiceShellStuff( Scope s ){


        s.put( "connect" , new JSFunctionCalls2(){
                public Object call( Scope s , Object name , Object ip , Object crap[] ){
                    String key = ip + ":" + name;
                    DBJni db = _dbs.get( key );
                    if ( db != null )
                        return db;
                    
                    db = DBJni.get( name.toString() , ip == null ? null : ip.toString() );
                    _dbs.put( key , db );
                    return db;
                }
                
                Map<String,DBJni> _dbs = new HashMap<String,DBJni>();
            } , true  );

        ed.db.migrate.Drivers.init( s );
        
        s.put( "openFile" , new JSFunctionCalls1(){
                public Object call( Scope s , Object fileName , Object crap[] ){
                    return new JSLocalFile( fileName.toString() );
                }
            } , true );

        s.put( "core" , new JSFileLibrary( new File( "/data/corejs" ) ,  "core" ) , true );
            
    }
    
    public static void main( String args[] )
        throws Exception {
        
        //System.setOut( new PrintStream( _myOutputStream ) );
        
        Scope s = Scope.GLOBAL.child();
        
        addNiceShellStuff( s );

        File init = new File( System.getenv( "HOME" ) + "/.init.js" );

        if ( init.exists() )
            s.eval( init );
        
        for ( String a : args ){
            File temp = new File( a );
            try {
                s.eval( temp );
            }
            catch ( Exception e ){
                ((JSFileLibrary)s.get("core")).fix( e );
                e.printStackTrace();
                return;
            }

        }

        String line;
        ConsoleReader console = new ConsoleReader();
        console.setHistory( new History( new File( ".jsshell" ) ) );
        
        boolean hasReturn[] = new boolean[1];
        
        while ( ( line = console.readLine( "> " ) ) != null ){
            line = line.trim();
            if ( line.length() == 0 )
                continue;
            try {
                Object res = s.eval( line , "lastline" , hasReturn );
                if ( hasReturn[0] )
                    System.out.println( JSON.serialize( res ) );
            }
            catch ( Exception e ){
                ((JSFileLibrary)s.get("core")).fix( e );
                e.printStackTrace();
                System.out.println();
            }
        }
    }
}
