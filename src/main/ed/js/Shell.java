// Shell.java

package ed.js;

import java.io.*;
import java.util.*;

import jline.*;

import ed.db.*;
import ed.js.func.*;
import ed.js.engine.*;

public class Shell {

    public static void addNiceShellStuff( Scope s ){


        s.put( "connect" , new JSFunctionCalls2(){
                public Object call( Scope s , Object name , Object ip , Object crap[] ){
                    String key = ip + ":" + name;
                    DBJni db = _dbs.get( key );
                    if ( db != null )
                        return db;
                    
                    db = new DBJni( name.toString() , ip == null ? null : ip.toString() );
                    _dbs.put( key , db );
                    return db;
                }
                
                Map<String,DBJni> _dbs = new HashMap<String,DBJni>();
            } , true  );
        
        s.put( "openFile" , new JSFunctionCalls1(){
                public Object call( Scope s , Object fileName , Object crap[] ){
                    return new JSNewFile.Local( fileName.toString() );
                }
            } , true );
        
            
    }
    
    public static void main( String args[] )
        throws Exception {
        
        Scope s = Scope.GLOBAL.child();

        addNiceShellStuff( s );

        File init = new File( System.getenv( "HOME" ) + "/.init.js" );
        System.out.println( init );
        if ( init.exists() )
            s.eval( init );
        
        for ( String a : args ){
            File temp = new File( a );
            s.eval( temp );
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
                e.printStackTrace();
                System.out.println();
            }
        }
    }
}
