// JSLocalFile.java

package ed.js;

import ed.io.*;
import ed.util.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

public class JSLocalFile extends JSNewFile {
    public JSLocalFile( String s ){
        this( new File( s ) );
    }
    
    public JSLocalFile( File f , String s ){
        this( new File( f , s ) );
    }

    public JSLocalFile( File f ){
        super( f.getName() , ed.appserver.MimeTypes.get( f ) , f.length() );
        _file = f;
    }

    // ----- 
    
    protected JSFileChunk newChunk( int i ){
        return new MyChunk( i );
    }
    
    class MyChunk extends JSFileChunk {
        MyChunk( int num ){
            super( JSLocalFile.this , num );
            _num = num;
        }
        
        public JSBinaryData getData(){
            final long start = _num * getChunkSize();
            final long end = Math.min( _file.length() , ( _num + 1 ) * getChunkSize() );
            
            return new JSBinaryData(){
                
                public int length(){
                    return (int)( end - start );
                }
                
                public void put( ByteBuffer buf ){
                    
                    try {
                            
                        if ( _fc == null )
                            _fc = (new FileInputStream( _file )).getChannel();
                            
                        final int oldLimit = buf.limit();
                        buf.limit( buf.position() + Math.min( getChunkSize() , length() ) );
                            
                        _fc.read( buf , _num * getChunkSize() );
                        buf.limit( oldLimit );
                    }
                    catch ( IOException ioe ){
                        throw new RuntimeException( "can't read file " + _file , ioe );
                    }
                }
                    
                public void write( OutputStream out ) 
                    throws IOException {

                    byte b[] = new byte[length()];
                    ByteBuffer bb = ByteBuffer.wrap( b );
                    
                    put( bb );
                    
                    out.write( b );
                }
                    
                public ByteBuffer asByteBuffer(){
                    ByteBuffer bb = ByteBuffer.allocateDirect( length() );
                    put( bb );
                    bb.flip();
                    return bb;
                }
                
            };
        }
            
        final int _num;
    }

    // ----- 

    public String getDataAsString(){
	try {
	    return StreamUtil.readFully( new FileInputStream( _file ) );
	}
	catch ( IOException ioe ){
	    throw new JSException( "couldn't read : " + _file , ioe );
	}
    }

    public String getName(){
        return _file.getName();
    }

    public boolean exists(){
        return _file.exists();
    }

    public boolean isDirectory(){
        return _file.isDirectory();
    }

    public long length(){
        return _file.length();
    }

    public File getRealFile(){
        return _file;
    }

    public boolean remove(){
        return remove( false );
    }

    public boolean remove( boolean recursive ){
        if ( ! _file.exists() )
            return true;
        
        if ( ! isDirectory() || ! recursive )
            return _file.delete();
        
        return _delete( _file );
    }
        
    private boolean _delete( File f ){
        if ( ! f.exists() )
            return true;
        
        if ( ! f.isDirectory() )
            return f.delete();
        
        for ( File c : f.listFiles() ){
            if ( ! _delete( c ) )
                return false;
        }

        return f.delete();
    }

    public boolean mkdirs(){
        return _file.mkdirs();
    }

    public JSArray listFiles(){
        JSArray a = new JSArray();
        for ( File f : _file.listFiles() )
            a.add( new JSLocalFile( f ) );
        return a;
    }

    public JSDate lastModified(){
        return new JSDate( _file.lastModified() );
    }

    public void renameTo( JSLocalFile f ){
        _file.renameTo( f._file );
    }

    public boolean touch(){
        try {
            _file.createNewFile();
            return true;
        }
        catch ( IOException ioe ){
            return false;
        }
    }

    final File _file;
    int _curChunk = 0;
    FileChannel _fc;
}

