// JSFileChunk.java

package ed.js;

import java.util.*;

import ed.db.*;

/** @expose */
public class JSFileChunk extends JSObjectBase {

    /**  Create a file chunk for a given file
     * @param f File for which to create a chunk.
     * @param chunkNumber Chunk to be created.
     */
    public JSFileChunk( JSFile f , int chunkNumber ){
        this();
        set( "cn" , chunkNumber );
    }

    /** Set the database namespace for this chunk to _chunks */
    public JSFileChunk(){
        set( "_ns" , "_chunks" );
    }

    /** Set this chunk's data to the given data.
     * @param data
     */
    public void setData( JSBinaryData data ){
        set( "data" , data );
    }

    /** Return this chunk's data.
     * @return This chunk's data.
     * @throws RuntimeException If this chunk does not have a data field.
     */
    public JSBinaryData getData(){
        if ( get( "data" ) != null )
            return (JSBinaryData)get("data");

        throw new RuntimeException( "don't have any data :(" );
    }

    /** Get the next chunk.
     * @return The next chunk.
     */
    public JSFileChunk getNext(){
        JSObject n = (JSObject)get( "next" );
        if ( n == null )
            return null;
        n.keySet();

        return (JSFileChunk)(get( "next" ));
    }

    /** Return a collection of the keys in this chunk.  If "data" is null, try to set it.
     * @return Collection of keys in this chunk.
     * @throws RuntimeException If this chunk has no chunk number.
     */
    public Collection<String> keySet(){
        if ( get( "cn" ) == null )
            throw new RuntimeException( "bad chunk" );

        if ( get( "data") == null )
            set( "data" , getData() );
        return super.keySet();
    }

    public static void setup( DBCollection db ){
        db.ensureIDIndex();
    }


}
