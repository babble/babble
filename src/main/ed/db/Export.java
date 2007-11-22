// Export.java

package ed.db;

import java.io.*;
import java.util.*;

import ed.js.*;

public class Export {
    
    static void export( String ip , OutputStream raw )
        throws IOException {

        PrintStream out = new PrintStream( raw );
        
        DBJni system = DBJni.get( "system" , ip );
        DBCollection namespaces = system.getCollection( "namespaces" );

        Map<String,List<String>> m = new HashMap<String,List<String>>();
        
        for ( Iterator<JSObject> i = namespaces.find( new JSObjectBase() , null , 0 ) ; i.hasNext() ;  ){
            JSObject o = i.next();
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

            DBJni db = DBJni.get( root , ip );
            out.println( "var " + root + " = connect( \"" + root + "\" );" );
            
            List<String> tables = m.get( root );
            for ( String t : tables ){
		if ( t.indexOf( ".$" ) >= 0 )
		    continue;
                DBCollection c = db.getCollection( t );
                out.println( "var t = " + root + "." + t + ";" );
                
                Iterator<JSObject> all = c.find( new JSObjectBase() , null , 0 );
                for( ; all.hasNext(); ){
                    JSObject o = all.next();
                    String nice = JSON.serialize( o , "" ).replace( '\r' , ' ' );
                    nice = nice.replaceAll( "\n" , "\\\\n" );
                    out.println( "t.save( " + nice  + ");" );
                }
                
            }
        }
    }

    public static void main( String args[] )
        throws Exception {

        String ip = "127.0.0.1";
        if ( args.length > 0 )
            ip = args[0];
        
        OutputStream out = System.out;
        if ( args.length > 1 )
            out = new FileOutputStream( args[1] );
        
        export( ip , out );
        
    }
    
}
