// ExportBinary.java

package ed.db;

import java.io.*;
import java.nio.channels.*;
import java.util.*;

import ed.js.*;

public class ExportBinary {
    
    static void export( String root , String ip , File baseDir )
        throws IOException {

        File myRoot = new File( baseDir , root );
        myRoot.mkdirs();

        DBApiLayer db = DBProvider.get( root , ip );
        
        for ( String t : db.getCollectionNames() ){
	    
            if ( t.indexOf( ".$" ) >= 0 )
                continue;
	    
            System.out.println( "Exporting : full ns : " + root + "." +  t );

            DBCollection c = db.getCollection( t );
            
	    File f = new File( myRoot , t + ".bin" );
	    try {
		export( c , f );
	    }
	    catch( Exception e ){
		System.err.println( "\t error" );
		e.printStackTrace();
	    }
        }
    }

    public static void export( DBCollection c , File f )
	throws IOException {

        ByteEncoder encoder = ByteEncoder.get();

	FileOutputStream fout = new FileOutputStream( f );
	FileChannel fc = fout.getChannel();
	
	Iterator<JSObject> all = c.find( new JSObjectBase() , null , 0 , 0 );
	if ( all == null )
	    return;
	
	while( all.hasNext() ){
	    JSObject o = all.next();
            
	    encoder.reset();
	    encoder.putObject( o );
	    encoder.flip();
            
	    fc.write( encoder._buf );
	}
	
	fout.close();

	encoder.done();
    }

    public static void main( String args[] )
        throws Exception {

        String ip = args[0];

        File dir = new File( args[1] );
        dir.mkdirs();
        
        if ( args.length > 2 ){
            for ( int i=2; i<args.length; i++ )
                export( args[i] , ip , dir );
        }
        else {
            for ( String root : DBJni.getRootNamespaces( ip ) ){
                export( root , ip , dir );
            }
        }
        
    }
    
}
