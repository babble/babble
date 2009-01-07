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

        if (cmd.endsWith(";")) {
            cmd = cmd.substring(0,cmd.length()-1);
        }
        if ( cmd.equalsIgnoreCase( "tables" ) || cmd.equalsIgnoreCase( "collections" ) ){
            for ( String c : _db.getCollectionNames() ){
                _out.println( c );
            }
            return;
        }
        if ( cmd.equalsIgnoreCase( "dbs" ) ){
            DBBase admin = DBProvider.getSisterDB( _db , "admin" );
            JSObject res = admin.getCollection( "$cmd" ).findOne( JSDictBuilder.start().set( "listDatabases" , 1 ).get() );
            if ( ! ( res.get( "ok" ) instanceof Number) ||
                 ((Number)res.get( "ok" )).intValue() != 1 ){
                _out.println( "error : " + JSON.serialize( res ) );
                return;
            }

            for ( Object o : (List)(res.get( "databases" ) ) ){
                _out.println( ((JSObject)o).get( "name" ) );
            }
            return;
        }

        
        _out.println( "don't know how to show [" + cmd + "]" );
    }
    
    void _iterateCursor( boolean expectMore ){
        DBCursor c = (DBCursor)_scope.get( "last" );
        if ( c == null ){
            _out.printf( "no cursor to iterate\n" );
            return;
        }
        
        _displayCursor( c );
    }
    
    void _showHelp(){
        _out.println( "HELP" );
        _out.println( "\t" + "show (dbs|collections)" );
        _out.println( "\t" + "db.foo.find()" );
        _out.println( "\t" + "db.foo.find( { a : 1 } )" );
    }
    
    static String _string( Object val ){
        if ( val == null )
            return "null";
        
        if ( val instanceof DBRef )
            return "DBRef";
        
        if ( val instanceof JSDate )
            return ((JSDate)val).strftime( "%D %T" );

        String s = val.toString();
        if ( s.length() > 30 )
            return s.substring( 0 , 27 ) + "...";
        return s;
    }
    
    int _displayCursor( DBCursor c ){
        return displayCursor( _out , c );
    }
    
    public static int displayCursor( PrintStream out , DBCursor c ){
        
        List<JSObject> all = new ArrayList<JSObject>();
        Map<String,Integer> fields = new TreeMap<String,Integer>();

        for ( int i=0; i<30 && c.hasNext() ; i++ ){
            JSObject obj = c.next();
            all.add( obj );
            
            for ( String f : obj.keySet( false ) ){
                
                if ( JSON.IGNORE_NAMES.contains( f ) )
                    continue;

                Object blah = _get( obj , f );
                
                Integer old = fields.get( f );
                if ( old == null )
                    old = 4;
                
                fields.put( f , Math.max( f.length() , Math.max( old , _string( blah ).length() ) ) );
            }
        }
        
        if ( all.size() == 0 )
            return 0;
        
        for ( String f : fields.keySet() ){
            out.printf( "%" + fields.get( f ) + "s | " , f );
        }
        out.printf( "\n" );

        for ( JSObject obj : all ){
            for ( String f : fields.keySet() ){
                out.printf( "%" + fields.get( f ) + "s | " , _string( _get( obj , f ) ) );
            }   
            out.printf( "\n" );
        }
        
        return all.size();
    }

    static Object _get( JSObject o , String n ){
        if ( o instanceof JSObjectBase )
            return ((JSObjectBase)o)._simpleGet( n );
        return o.get( n );
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
            
            if ( line.equalsIgnoreCase( "exit" ) )
                break;
            
            if ( line.equalsIgnoreCase( "help" ) ){
                _showHelp();
                continue;
            }

            if ( line.toLowerCase().startsWith( "show " ) ){
                _handleShow( line.substring( 5 ).trim() );
                continue;
            }

            if ( line.toLowerCase().startsWith( "use " ) ){
                String newDB = line.substring( 4 ).trim();
                _out.println( "switching to [" + newDB + "]" );
                _db = DBProvider.getSisterDB( _db , newDB );
                continue;
            }
            
            try {
                Object res = _scope.eval( line );
                
                if ( res instanceof DBCursor ){
                    _scope.put( "last" , res , true );
                    _displayCursor( (DBCursor)res );
                    continue;
                }
    
                _out.println( JSON.serialize( res ) );
            }
            catch(RuntimeException e) { 
                if (e.getMessage().startsWith("can't compile")) {
                    _out.println("Error in command.");
                }
                else { 
                    e.printStackTrace(_out);
                }
            }
            
        }
        
    }

    DBBase _db;
    final PrintStream _out;
    final Scope _scope;
    
    public static void main( String args[] )
        throws IOException {

        String dbName = "test";

        if ( args.length > 0 )
            dbName = args[0];
        
        Shell s = new Shell( System.out , DBProvider.get( dbName ) );
        s.repl();
    }


    
}
