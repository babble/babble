// ImportBinary.java

package ed.db;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import org.apache.commons.cli.*;

import ed.js.*;

public class ImportBinary {

    static boolean debug = false;
    static boolean onlyids = false;
    static boolean forceUpdate = false;
    static boolean forceSave = false;
    
    static void load( File root )
        throws IOException {
        
        if ( ! root.exists() )
            throw new RuntimeException( root + " does not exist" );

        for ( File f : _list( root ) ){
            
            if ( f.isDirectory() ){
                loadNamespace( f );
                continue;
            }

            if ( f.toString().endsWith( ".bin" ) ){
                loadOne( f );
                continue;
            }
        }
    }

    static void loadNamespace( File dir )
        throws IOException {
        
        if ( ! dir.exists() )
            throw new RuntimeException( dir + " does not exist" );

        for ( File f : _list( dir ) ){
            if ( f.isDirectory() )
                continue;
            loadOne( f );
        }
    }

    static void loadOne( File f )
        throws IOException {

        if ( true ){
            System.out.println( f );
            return;
        }


        if ( ! f.exists() || f.isDirectory() )
            throw new RuntimeException( f + " must be a regular fule" );

        final String ns = f.getName().replaceAll( "\\.bin$" , "" );
        final String root = f.getParentFile().getName();
        
        DBApiLayer db = DBProvider.get( root );
        DBCollection coll = db.getCollection( ns );
        
        System.out.println( "full ns : " + root + "." + ns );

        FileInputStream fin = new FileInputStream( f );

        ByteBuffer bb = ByteBuffer.allocateDirect( (int)(f.length()) );
        bb.order( ByteOrder.LITTLE_ENDIAN );
        
        FileChannel fc = fin.getChannel();
        fc.read( bb );
        
        bb.flip();
        ByteDecoder decoder = new ByteDecoder( bb );
        
        boolean hasAny = false;
        {
            Iterator<JSObject> checkForAny = coll.find( new JSObjectBase() , null , 0 , 2 );
            if ( checkForAny != null && checkForAny.hasNext() )
                hasAny = true;
        }
        
	if ( debug ) System.out.println( "\t hasAny " + hasAny + " (implies doing update unless forceSave" );

        int num = 0;
        boolean skippedAny = false;

        JSObject o;
        while ( ( o = decoder.readObject() ) != null ){
            
            if ( onlyids && o.get( "_id" ) == null ){
                if ( ! skippedAny ){
                    skippedAny = true;
                    if ( debug ) 
                        System.out.println( "\t skipping something b/c it doesn't have id.  only msg once per collection" );
                }
                continue;
            }

            if ( forceUpdate )
                coll.save( o );
            else if ( forceSave )
                coll.doSave( o );
            else if ( hasAny )
                coll.save( o );
            else
                coll.doSave( o );

            if ( ++num % 10000 == 0 ){
                if ( debug ) System.out.println( "\t " + num );
            }
        }
        
        System.out.println( "\t total : " + num );

        coll.find( new JSObjectBase() , null , 0 , 1 );
    }

    static File[] _list( File f ){
        File lst[] = f.listFiles();
        Arrays.sort( lst , _fileComparator );
        return lst;
    }

    public final static Comparator<File> _fileComparator = new Comparator<File>(){
            public int compare( File lf , File rf ){
                String l = lf.getName();
                String r = rf.getName();

                if ( l.equals( "system.indexes.bin"  ) )
                    return 1;

                if ( r.equals( "system.indexes.bin"  ) )
                    return -1;

                return l.compareTo( r );
                
            }
            public boolean equals(Object obj ){
                return this == obj;
            }
        };

    public static void main( String args[] )
        throws Exception {

        Options o = new Options();
        o.addOption( "forceUpdate" , false , "Force update instead of save" );
        o.addOption( "forceSave" , false , "Force save instead of update" );
        o.addOption( "v" , false , "Verbose" );
        o.addOption( "onlyids"  , false , "only do objects with ids" );
        
        CommandLine cl = ( new BasicParser() ).parse( o , args );
        
        if ( cl.getArgList().size() == 0 ){
            System.out.println( o );
            return;
        }
        
        if ( cl.hasOption( "v" ) )
            debug = true;
        
        if ( cl.hasOption( "onlyids" ) )
            onlyids = true;
        
        if ( cl.hasOption( "forceUpdate" ) )
            forceUpdate = true;

        if ( cl.hasOption( "forceSave" ) )
            forceSave = true;

        if ( forceUpdate && forceSave )
            throw new RuntimeException( "can't force update and save !" );

        load( new File( cl.getArgList().get(0).toString() ) );
        
    }
}


