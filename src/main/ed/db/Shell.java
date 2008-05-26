// Shell.java

package ed.db;

import java.io.*;
import java.util.*;

import jline.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;

public class Shell {
    
    Shell( PrintStream out , DBBase db ){
        _out = out;
        _db = db;
        _scope = Scope.newGlobal().child();
        _scope.put( "db" , _db , true );
    }

    void _handleShow( String cmd ){

        if ( cmd.equalsIgnoreCase( "tables" ) || cmd.equalsIgnoreCase( "collections" ) ){
            for ( String c : _db.getCollectionNames() ){
                _out.println( c );
            }
            return;
        }
        
        _out.print( "don't know how to show [" + cmd + "]" );
    }
    
    void _iterateCursor( boolean expectMore ){
        DBCursor c = (DBCursor)_scope.get( "last" );
        if ( c == null ){
            _out.printf( "no cursor to iterate\n" );
            return;
        }
        
        _displayCursor( c );
    }
    
    String _string( Object val ){
        if ( val == null )
            return "null";
        String s = val.toString();
        if ( s.length() > 30 )
            return s.substring( 0 , 27 ) + "...";
        return s;
    }
    
    int _displayCursor( DBCursor c ){
        
        List<JSObject> all = new ArrayList<JSObject>();
        Map<String,Integer> fields = new TreeMap<String,Integer>();

        for ( int i=0; i<30 && c.hasNext() ; i++ ){
            JSObject obj = c.next();
            all.add( obj );
            
            for ( String f : obj.keySet() ){
                
                if ( JSON.IGNORE_NAMES.contains( f ) )
                    continue;

                Object blah = obj.get( f );
                
                Integer old = fields.get( f );
                if ( old == null )
                    old = 0;
                
                fields.put( f , Math.max( old , _string( blah ).length() ) );
            }
        }
        
        if ( all.size() == 0 )
            return 0;
        
        for ( String f : fields.keySet() ){
            _out.printf( "%" + fields.get( f ) + "s | " , f );
        }
        _out.printf( "\n" );

        for ( JSObject obj : all ){
            for ( String f : fields.keySet() ){
                _out.printf( "%" + fields.get( f ) + "s | " , _string( obj.get( f ) ) );
            }   
            _out.printf( "\n" );
        }
        
        return all.size();
    }

    public void repl()
        throws IOException {
        
        ConsoleReader console = new ConsoleReader();
        console.setHistory( new History( new File( ".jsdbshell" ) ) );
        
        String line;
        while ( ( line = console.readLine( "> " ) ) != null ){
            line = line.trim();
            if ( line.length() == 0 || 
                 line.equalsIgnoreCase( "n" ) ||
                 line.equalsIgnoreCase( "next" )
                 ){
                _iterateCursor( line.length() > 0 );
                continue;
            }
            
            if ( line.equals( "exit" ) )
                break;
            
            if ( line.toLowerCase().startsWith( "show " ) ){
                _handleShow( line.substring( 5 ).trim() );
                continue;
            }
            
            Object res = _scope.eval( "db." + line );
            
            if ( res instanceof DBCursor ){
                _scope.put( "last" , res , true );
                _displayCursor( (DBCursor)res );
                continue;
            }

            _out.println( res );
            
        }
        
    }

    final PrintStream _out;
    final DBBase _db;
    final Scope _scope;

    public static void main( String args[] )
        throws IOException {

        if ( args.length < 1 ){
            System.err.println( "usage: ed.db.Shell [db name] <host name>" );
            System.exit(-1);
        }
        
        String dbName = args[0];
        String hostName = args.length > 1 ? args[1] : null;
        
        Shell s = new Shell( System.out , DBProvider.get( dbName , hostName ) );
        s.repl();
    }


    
}