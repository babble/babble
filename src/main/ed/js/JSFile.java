// JSFile.java

/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package ed.js;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

import ed.db.*;
import ed.io.*;
import ed.js.*;
import ed.js.engine.*;

/**
 * The system stores files as a linked list of chunks.  Thus, to delete a file, one must delete each chunk.
 * @expose
 */
public abstract class JSFile extends JSObjectBase {

    /** Default chunk size: 1024 * 256 */
    protected static final int DEF_CHUNK_SIZE = 1024 * 256;

    /** @unexpose */
    protected JSFile(){
        set( "_ns" , "_files" );
    }

    /** @unexpose */
    protected JSFile( String filename , String contentType , long length ){
        this( null , filename , contentType , length );
    }

    /** @unexpose */
    protected JSFile( ObjectId id , String filename , String contentType , long length ){
        this();

        filename = cleanFilename( filename );

        if ( id != null )
            set( "_id" , id );

        if ( contentType == null && filename != null )
            contentType = ed.appserver.MimeTypes.get( filename );

        set( "filename" , filename );
        set( "contentType" , contentType );
        set( "length" , length );
    }

    /** Returns the first chunk for this file.
     * @return The first chunk for this file or null if this file has 0 length.
     * @throws NullPointerException If the first chunk is there but null.
     */
    public JSFileChunk getFirstChunk(){
        if ( getLength() == 0 )
            return null;

        ((JSObject)get( "next" )).keySet();
        JSFileChunk chunk = (JSFileChunk)get( "next" );
        if ( chunk == null )
            throw new NullPointerException( "first chunk is null :(" );
        return chunk;
    }

    /** Write a stream to this file.
     * @param out Stream to write.
     * @throws IOException
     */
    public void write( OutputStream out )
        throws IOException {

        JSFileChunk chunk = getFirstChunk();
        while ( chunk != null ){
            chunk.getData().write( out );
            chunk = chunk.getNext();
        }
    }

    /** Returns this file's filename.
     * @return This file's filename.
     */
    public String getFileName(){
        return getJavaString( "filename" );
    }

    /** Returns this file's content type.
     * @return This file's content type.
     */
    public String getContentType(){
        return getJavaString( "contentType" );
    }

    /** Returns this file's content disposition, which could be inline, attachment, or extension-token.
     * @return This file's content disposition.
     */
    public String getContentDisposition(){
        String s = getJavaString( "contentDisposition" );
        if ( s != null )
            return s;

        return ed.appserver.MimeTypes.getDispositionFromMimeType( getContentType() );
    }

    /** Return the length of this file in bytes.
     * @return This file's length.
     */
    public long getLength(){
        return ((Number)get( "length" )).longValue();
    }

    /** Return the number of chunks this file is stored over.
     * @return The number of chunks used for this file.
     */
    public int numChunks(){
        return (int)Math.ceil( (double)getLength() / getChunkSize() );
    }

    /** Return the chunk size this file uses.
     * @return The chunk size used.
     */
    public int getChunkSize(){
        Object foo = get( "chunkSize" );
        if ( foo == null ){
            set( "chunkSize" , DEF_CHUNK_SIZE );
            return DEF_CHUNK_SIZE;
        }
        return ((Number)foo).intValue();
    }

    /** Returns the date that this file was uploaded.
     * @return The date that this file was uploaded.
     */
    public JSDate getUploadDate(){
        JSDate d = (JSDate)get( "uploadDate" );
        if ( d != null )
            return d;
        return new JSDate();
    }
    
    /**
     * @return mutable array of aliases or null if there are none
     */
    public JSArray getAliases(){
        return (JSArray)get( "aliases" );
    }
    
    /**
     * adds an alias for this file
     */
    public void addAlias( String newAlias ){
        JSArray a = (JSArray)get("aliases");
        if ( a == null ){
            a = new JSArray();
            set( "aliases" , a );
        }
        a.add( newAlias );
    }

    /** Returns this file's contents as a string.
     * @return This file's contents as a string.
     * @throws IOException
     */
    public String asString(){
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        WritableByteChannelConnector w = new WritableByteChannelConnector( bout );
        Sender s = sender();
        try {
            while ( ! s.write( w ) );
        }
        catch ( IOException ioe ){
            throw new RuntimeException( "should be impossible" , ioe );
        }
        return new String( bout.toByteArray() );
    }

    /** Returnshis file's filename.
     * @return This file's filename.
     */
    public String toString(){
        return getFileName();
    }

    /** Create an input stream to send the file.
     * @return A modified input stream
     */
    public Sender sender(){
        return new Sender( getFirstChunk() );
    }

    /** Get a temporary directory suitable for the instance described by root.
     *  This has to be on the same filesystem as root, because we rename(2) a
     *  file from this temp directory to the proper location, and it has to be
     *  atomic.
     * @param root The desired temporary directory.
     * @returns a directory on the same filesystem as root
     * @throws RuntimeException If <tt>root</tt> exists but is not a directory
    */
    public static File getTempDirectory( File root ){
        // We try root + .git/tmp and then root + .tmp
        File git = new File( root , ".git" );
        File retTemp = null;
        if( git.exists() && git.isDirectory() ){
            File git_temp = new File( git , "tmp" );
            if( ! git_temp.exists() ){
                git_temp.mkdir();
            }
            retTemp = git_temp;
        }
        if( retTemp == null || ! retTemp.isDirectory() ){
            File site_temp = new File( root , ".tmp" );
            if( ! site_temp.exists() )
                site_temp.mkdir();
            retTemp = site_temp;
        }
        if( ! retTemp.isDirectory() )
            throw new RuntimeException( "file exists but is not a directory : " + retTemp.toString() );

        return retTemp;
    }

    /** Create a file and any required parent directories.
     * @param name File path/name
     * @return full path
     * @throws IOException If the temporary file created cannot be renamed to <tt>name</tt>
     */
    public String writeToLocalFile( String name )
        throws IOException {

        Scope s = Scope.getThreadLocal();
        if ( s == null )
            throw new JSException( "need a scope" );

        File f = null;

        File root = (File)s.get( "_rootFile" );
        if ( root == null ){
            f = new File( name );
        }
        else {
            if( name.contains("/") ){
                File dir = new File( root , name.replaceAll( "/[^/]+$" , "/" ) );
                dir.mkdirs();
            }
            else {
                root.mkdirs();
            }

            f = new File( root , name );
        }

        File tempdir = null;
        if( root != null ){
            tempdir = JSFile.getTempDirectory(root);
        }

        File temp = File.createTempFile( "writeToLocalFile" , ".tmpaa" , tempdir );

        FileOutputStream out = new FileOutputStream( temp);
        write( out );
        out.close();

        if(temp.renameTo( f ) == false)
            throw new IOException("rename from " + temp.toString() + " to "+ f.toString() + " failed");

        return f.getAbsolutePath();
    }

    public class Sender extends InputStream {

        Sender( JSFileChunk chunk ){

            if ( chunk == null ){
                _chunk = null;
                _buf = null;
                return;
            }

            if ( chunk.getData() == null )
                throw new NullPointerException("chunk data can't be null" );
            _chunk = chunk;
            _buf = _chunk.getData().asByteBuffer();
        }

        /**
         * @return true if we're all done
         */
        boolean _done()
            throws IOException {

            if ( _maxPostion >= 0 && _bytesWritten > _maxPostion )
                return true;

            if ( _chunk == null )
                return true;

            if ( _buf.remaining() > 0 )
                return false;

            _buf = null;
            _chunk = _chunk.getNext();

            if ( _chunk == null )
                return true;

            _buf = _chunk.getData().asByteBuffer();

            if ( _maxPostion > 0 ){
                long bytesLeft = _maxPostion - _bytesWritten;

                if ( ( _buf.limit() - _buf.position() ) > bytesLeft )
                    _buf.limit( _buf.position() + (int)bytesLeft );
            }

            return false;
        }

        /**
         * @return true if we're all done
         */
        public boolean write( WritableByteChannel out )
            throws IOException {

            if ( _done() )
                return true;

            _bytesWritten += out.write( _buf );
            return false;
        }

        public long skip( final long num )
            throws IOException {
            if ( num <= 0 )
                return 0;

            final long start = _bytesWritten;


            WritableByteChannel out = new WritableByteChannel(){
                    public int write ( ByteBuffer src )
                        throws IOException {
                        for ( long i=0; i<num; i++ )
                            src.get();
                        return (int)num;
                    }

                    public void close(){}
                    public boolean isOpen(){ return true; }

                };

            while ( _bytesWritten - start < num && ! write( out ) );

            return _bytesWritten - start;
        }

        public void maxPosition( long max ){
            _maxPostion = max;
        }

        public int available(){
            return (int)(getLength() - _bytesWritten);
        }

        public void close(){
            // NO-OP
        }

        public int read(){
            throw new RuntimeException( "not supported" );
        }

        public int read(byte[] b)
            throws IOException {
            return read( b , 0 , b.length );
        }

        public int read(byte[] b, int off, int len)
            throws IOException {

            if ( _done() )
                return -1;

            final int toCopy = Math.min( len , _buf.remaining() );

            _buf.get( b , off , toCopy );
            _bytesWritten += toCopy;

            return toCopy;
        }

        public void mark(int readlimit){
            throw new RuntimeException( "not supported" );
        }
        public boolean markSupported(){
            return false;
        }
        public void reset(){
            throw new RuntimeException( "not supported" );
        }

        JSFileChunk _chunk;
        ByteBuffer _buf;
        long _bytesWritten = 0;
        long _maxPostion = -1;
    }

    /** Given a path, get rid of everything other than the filename. (For example, cleanFilename("/x/y/z/log.out") would return "log.out").
     * @param filename Filename to be cleaned.
     * @return Cleaned filename.
     */
    public static String cleanFilename( String filename ){
        if ( filename == null )
            return null;

        int idx = filename.lastIndexOf( "/" );
        if ( idx > 0 )
            filename = filename.substring( idx + 1 );

        idx = filename.lastIndexOf( "\\" );
        if ( idx > 0 )
            filename = filename.substring( idx + 1 );

        return filename;
    }

    public static void setup( DBCollection db ){
        db.ensureIDIndex();
        
        JSObject o = new JSObjectBase();
        o.set( "aliases" , 1 );
        db.ensureIndex( o );

        o = new JSObjectBase();
        o.set( "filename" , 1 );
        db.ensureIndex( o );

        o = new JSObjectBase();
        o.set( "uploadDate" , 1 );
        db.ensureIndex( o );
        
    }

}
