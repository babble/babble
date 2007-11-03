// Export.java

package ed.db;

import java.io.*;
import java.util.*;

import ed.js.*;

public class Export {
    
    static void export( String ip , OutputStream raw )
        throws IOException {

        PrintStream out = new PrintStream( raw );
        
        DBJni system = new DBJni( "system" , ip );
        DBCollection namespaces = system.getCollection( "namespaces" );

        Map<String,List<String>> m = new HashMap<String,List<String>>();
        
        for ( JSObject o : namespaces.find( new JSObjectBase() ) ){
            String n = o.get( "name" ).toString();
            int idx = n.indexOf( "." );
            
            String root = n.substring( 0 , idx );
            String table = n.substring( idx + 1 );
            
            List<String> lst = m.get( root );
            if ( lst == null ){
                lst = new ArrayList<String>();
                m.put( root , lst );
            }
            lst.add( table );
        }
        
        
        for ( String root : m.keySet() ){
            
            if ( root.equals( "sys" ) )
                continue;

            DBJni db = new DBJni( root , ip );
            out.println( "var " + root + " = db." + root + ";" );
            
            List<String> tables = m.get( root );
            for ( String t : tables ){
                DBCollection c = db.getCollection( t );
                out.println( "var t = " + root + "." + t + ";" );
                
                List<JSObject> all = c.find( new JSObjectBase() );
                for( JSObject o : all ){
                    String nice = JSON.serialize( o ).replace( '\r' , ' ' );
                    nice = nice.replaceAll( "\n" , "\\\\n" );
                    out.println( "t.save( " + nice  + ");" );
                }
                
            }
        }
    }

    public static void main( String args[] )
        throws Exception {

        export( args[0] , System.out );
        
    }
    
}
