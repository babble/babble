// Export.java

package ed.db;

import java.io.*;
import java.util.*;

import ed.js.*;

public class Export {
    
    static void export( String ip , OutputStream raw )
        throws IOException {

        PrintStream out = new PrintStream( raw );
        
        for ( String root : DBApiLayer.getRootNamespaces( ip ) ){
            export( root , ip , out );
        }
    }

    static void export( String root , String ip , PrintStream out )
        throws IOException {
        DBApiLayer db = DBProvider.get( root , ip );
        out.println( "var " + root + " = connect( \"" + root + "\" );" );
        
        for ( String t : db.getCollectionNames() ){
            
            if ( t.indexOf( ".$" ) >= 0 )
                continue;
            
            DBCollection c = db.getCollection( t );
            out.println( "var t = " + root + "." + t + ";" );
            
            Iterator<JSObject> all = c.find( new JSObjectBase() , null , 0 , 0 );
            if ( all == null )
                continue;

            for( ; all.hasNext(); ){
                JSObject o = all.next();
                String nice = JSON.serialize( o , "" ).replace( '\r' , ' ' );
                nice = nice.replaceAll( "\n" , "\\\\n" );
                out.println( "t.save( " + nice  + ");" );
            }
        }
    }

    public static void main( String args[] )
        throws Exception {

        String ip = "127.0.0.1";
        if ( args.length > 0 )
            ip = args[0];
        
        if ( args.length <= 1 ){
            export( ip , System.out );
            return;
        }

        File dir = new File( args[1] );
        dir.mkdirs();
        
        for ( String root : DBApiLayer.getRootNamespaces( ip ) ){
            FileOutputStream raw = new FileOutputStream( new File( dir , root + ".txt" ) );
            PrintStream out = new PrintStream( raw );
            export( root , ip , out );
            out.close();
            raw.close();
        }
        
        
    }
    
}
