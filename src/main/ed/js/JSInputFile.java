// JSInputFile.java

package ed.js;

import java.io.*;
import java.util.*;

/** @expose  */
public class JSInputFile extends JSNewFile {

    /** Initializes a new input file.
     * @param filename Name for the file
     * @param contentType Type of file
     * @param content Contents of the file
     */
    public JSInputFile( String filename , String contentType , String content )
        throws IOException {
        this( filename , contentType , content.getBytes() );
    }

    /** Initializes a new input file.
     * @param filename Name for the file
     * @param b Contents of the file
     */
    public JSInputFile( String filename , byte b[] )
        throws IOException {
        this( filename , null , new ByteArrayInputStream( b ) );
    }

    /** Initializes a new input file.
     * @param filename Name for the file
     * @param contentType Type of file
     * @param b Contents of the file
     */
    public JSInputFile( String filename , String contentType , byte b[] )
        throws IOException {
        this( filename , contentType , new ByteArrayInputStream( b ) );
    }

    /** Initializes a new input file.
     * @param filename Name for the file
     * @param contentType Type of file
     * @param in Contents of the file
     */
    public JSInputFile( String filename , String contentType , InputStream in )
        throws IOException {
        this( filename , contentType , _read( in ) );
    }

    /** Initializes a new input file.
     * @param filename Name for the file
     * @param contentType Type of file
     * @param data Contents of the file
     */
    public JSInputFile( String filename , String contentType , List<JSBinaryData> data ){
        super( filename , contentType , _count( data ) );

        if ( data == null )
            throw new NullPointerException( "data can't be null" );

        _data = data;

        if ( DEF_CHUNK_SIZE != getChunkSize() )
            throw new RuntimeException( "uh oh" );
    }

    private static List<JSBinaryData> _read( InputStream in )
        throws IOException {

        List<JSBinaryData> data = new ArrayList<JSBinaryData>();

        byte cur[] = new byte[ DEF_CHUNK_SIZE ];
        int pos = 0;

        while ( true ){
            int l = in.read( cur , pos , cur.length - pos );

            if ( l < 0 )
                break;

            if ( l == 0 )
                continue;

            pos += l;

            if ( pos == cur.length ){
                data.add( new JSBinaryData.ByteArray( cur , 0 , cur.length ) );
                cur = new byte[ DEF_CHUNK_SIZE ];
                pos = 0;
            }
        }

        if ( pos > 0 )
            data.add( new JSBinaryData.ByteArray( cur , 0 , pos ) );

        return data;
    }

    private static long _count( List<JSBinaryData> data ){
        long total = 0;
        for ( JSBinaryData b : data )
            total += b.length();
        return total;
    }

    /** Returns a new file chunk.
     * @param i Number of the chunk to create
     */
    protected JSFileChunk newChunk( int i ){
        return new MyChunk( i );
    }

    class MyChunk extends JSFileChunk {
        MyChunk( int num ){
            super( JSInputFile.this , num );
            _num = num;
        }

        public JSBinaryData getData(){
            return _data.get( _num );
        }

        final int _num;
    }


    private final List<JSBinaryData> _data;
    private final List<MyChunk> _chunks = new ArrayList<MyChunk>();
}
