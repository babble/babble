// JSLocalFile.java

/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

package ed.js;

import ed.io.*;
import ed.util.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

/** @expose */
public class JSLocalFile extends JSNewFile {
    /** Initializes a new local file at a given location.
     * @param s File path
     */
    public JSLocalFile( String s ){
        this( new File( s ) );
    }

    /** Initializes a new local file at a given location.
     * @param f File from which to get the path
     * @param s Filename
     */
    public JSLocalFile( File f , String s ){
        this( new File( f , s ) );
    }

    /** Initializes a new local file.
     * @param f File to use
     */
    public JSLocalFile( File f ){
        super( f.getName() , ed.appserver.MimeTypes.get( f ) , f.length() );
        _file = f;
    }

    // -----

    /** Create a new file chunk for this file.
     * @param i The number of the file chunk to create
     * @return The new chunk
     */
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
    /** Return the contents of this file as a string.
     * @return The contents of this file
     * @throws JSException If the file couldn't be read
     */
    public String getDataAsString(){
    try {
        return StreamUtil.readFully( new FileInputStream( _file ) );
    }
    catch ( IOException ioe ){
        throw new JSException( "couldn't read : " + _file , ioe );
    }
    }

    /** Gets the filename.
     * @return This file's filename
     */
    public String getName(){
        return _file.getName();
    }

    /** Checks if this file exists.
     * @return If this file exists
     */
    public boolean exists(){
        return _file.exists();
    }

    /** Checks if this file is a directory.
     * @return if this file is a directory
     */
    public boolean isDirectory(){
        return _file.isDirectory();
    }

    /** The length of this file.
     * @return The length of this file
     */
    public long length(){
        return _file.length();
    }

    /** Returns this file object's file.
     * @return This file
     */
    public File getRealFile(){
        return _file;
    }

    /** Deletes this file.  If it is a directory, it does not delete it recursively.
     * @return if the file was successfully deleted
     */
    public boolean remove(){
        return remove( false );
    }

    /** Deletes a file or directory, using recursion if specified.
     * @param resursive If the contents of a directory should be deleted recursively
     * @return if the file or directory was successfully deleted
     */
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

    /** Make the directory this file's pathname specifies, including any parent directories.
     * @return true if and only if the directory was created, along with all necessary parent directories; false  otherwise
     */
    public boolean mkdirs(){
        return _file.mkdirs();
    }

    /** If this file is a directory, creates an array of files that are contained in this directory
     * @return An array of files
     */
    public JSArray listFiles(){
        JSArray a = new JSArray();
        File[] lst = _file.listFiles();
        if ( lst != null )
            for ( File f : _file.listFiles() )
                a.add( new JSLocalFile( f ) );
        return a;
    }

    /** Returns the time this file was last modified.
     * @return the time this file was last modified
     */
    public JSDate lastModified(){
        return new JSDate( _file.lastModified() );
    }

    /** Renames this file.
     * @param Path to which this file should be renamed
     * @return true if and only if the renaming succeeded; false otherwise
     */
    public void renameTo( JSLocalFile f ){
        _file.renameTo( f._file );
    }

    /** Updates the time modified, or creates this file if it doesn't exist.
     * @return if the touch succeeded
     */
    public boolean touch(){
        try {
            if( _file.createNewFile() )
                return true;

            if( _file.exists() )
                return _file.setLastModified( System.currentTimeMillis() );

            return false;
        }
        catch ( IOException ioe ){
            return false;
        }
    }

    public String getAbsolutePath(){
    return _file.getAbsolutePath();
    }

    final File _file;
    int _curChunk = 0;
    FileChannel _fc;
}
