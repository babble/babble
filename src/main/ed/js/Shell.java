// Shell.java

package ed.js;

import java.io.*;
import java.util.*;

import jline.*;

import ed.db.*;
import ed.js.func.*;
import ed.js.engine.*;

public class Shell {

    public static void main( String args[] )
        throws Exception {

        Scope s = Scope.GLOBAL.child();
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
        

        String line;
        ConsoleReader console = new ConsoleReader();
        console.setHistory( new History( new File( ".jsshell" ) ) );
        
        while ( ( line = console.readLine( "> " ) ) != null ){
            try {
                Convert c = new Convert( "lastline" , line );
                JSFunction f = c.get();
                Object res = f.call( s );
                if ( c.hasReturn() )
                    System.out.println( JSON.serialize( res ) );
            }
            catch ( Exception e ){
                e.printStackTrace();
                System.out.println();
            }
        }
    }
}
