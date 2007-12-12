// ExportBinary.java

package ed.db;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import ed.js.*;

public class ExportBinary {
    
    static void export( String root , String ip , File baseDir )
        throws IOException {

        ByteEncoder encoder = ByteEncoder.get();

        File myRoot = new File( baseDir , root );
        myRoot.mkdirs();

        DBJni db = DBJni.get( root , ip );
        
        for ( String t : db.getCollectionNames() ){
            
            if ( t.indexOf( ".$" ) >= 0 )
                continue;
            
            DBCollection c = db.getCollection( t );
            
            File f = new File( myRoot , t + ".bin" );
            FileOutputStream fout = new FileOutputStream( f );
            FileChannel fc = fout.getChannel();
            
            Iterator<JSObject> all = c.find( new JSObjectBase() , null , 0 );
            if ( all == null )
                continue;

            while( all.hasNext() ){
                JSObject o = all.next();

                encoder.reset();
                encoder.putObject( null , o );
                encoder.flip();
                
                fc.write( encoder._buf );
            }

            fout.close();
        }
    }

    public static void main( String args[] )
        throws Exception {

        String ip = args[0];

        File dir = new File( args[1] );
        dir.mkdirs();
        
        for ( String root : DBJni.getRootNamespaces( ip ) ){
            export( root , ip , dir );
        }
        
    }
    
}
