// JSFileChunk.java

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
    public Collection<String> keySet( boolean includePrototype ){
        if ( get( "cn" ) == null )
            throw new RuntimeException( "bad chunk" );

        if ( get( "data") == null )
            set( "data" , getData() );

        return super.keySet( includePrototype );
    }

    public static void setup( DBCollection db ){
        db.ensureIDIndex();
    }


}
