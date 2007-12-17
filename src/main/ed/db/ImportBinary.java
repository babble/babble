// ImportBinary.java

package ed.db;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import ed.js.*;

public class ImportBinary {

    static void load( File root )
        throws IOException {
        for ( File f : root.listFiles() ){
            if ( ! f.isDirectory() )
                continue;
            loadNamespace( f );
        }
    }

    static void loadNamespace( File dir )
        throws IOException {
        for ( File f : dir.listFiles() ){
            if ( f.isDirectory() )
                continue;
            loadOne( f );
        }
    }

    static void loadOne( File f )
        throws IOException {
        final String ns = f.getName().replaceAll( "\\.bin$" , "" );
        final String root = f.getParentFile().getName();
        
        DBJni db = DBJni.get( root );
        DBCollection coll = db.getCollection( ns );
        
        System.out.println( "full ns : " + root + "." + ns );

        FileInputStream fin = new FileInputStream( f );

        ByteBuffer bb = ByteBuffer.allocateDirect( (int)(f.length()) );
        bb.order( ByteOrder.LITTLE_ENDIAN );
        
        FileChannel fc = fin.getChannel();
        fc.read( bb );

        bb.flip();
        ByteDecoder decoder = new ByteDecoder( bb );
        
        JSObject o;
        while ( ( o = decoder.readObject() ) != null ){
            coll.save( o );
        }
        
    }

    public static void main( String args[] )
        throws Exception {
        
        load( new File( args[0] ) );
        
    }
}


