// JSFileChunk.java

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
    public Set<String> keySet( boolean includePrototype ){
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
