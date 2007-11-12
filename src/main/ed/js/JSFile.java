// JSFile.java

package ed.js;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

import ed.db.*;

public abstract class JSFile extends JSObjectBase {

    public static final int CHUNK_SIZE = 1024; // * 512;  making 1k for testing, will make 1 meg

    protected JSFile(){
        set( "_ns" , "_files" );
    }
    
    protected JSFile( String filename , String contentType , long length ){
        this( null , filename , contentType , length );
    }

    protected JSFile( ObjectId id , String filename , String contentType , long length ){
        this();
        if ( id != null )
            set( "_id" , id );
        
        set( "filename" , filename );
        set( "contentType" , contentType );
        set( "length" , length );
    }
    
    public JSFileChunk getFirstChunk(){
        ((JSObject)get( "next" )).keySet();
        return (JSFileChunk)get( "next" );
    }

    public void write( OutputStream out )
        throws IOException {
        
        JSFileChunk chunk = getFirstChunk();
        while ( chunk != null ){
            chunk.getData().write( out );
            chunk = chunk.getNext();
        }
    }

    public String getFileName(){
        return getJavaString( "filename" );
    }
    
    public String getContentType(){
        return getJavaString( "contentType" );
    }

    public long getLength(){
        return ((Number)get( "length" )).longValue();
    }
    
    public int numChunks(){
        System.out.println( "length : " + getLength() );
        System.out.println( "length / CHUNK_SIZE : " + ( getLength() / CHUNK_SIZE ) );
        return (int)Math.ceil( (double)getLength() / CHUNK_SIZE );
    }

    public String toString(){
        return "{ JSFile.  filename:" + getFileName() + " contentType:" + getContentType() + " length:" + getLength() + " }";
    }

}




