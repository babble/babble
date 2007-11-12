// JSFileChunk.java

package ed.js;

import java.util.*;

public abstract class JSFileChunk extends JSObjectBase {

    protected JSFileChunk( JSFile f , int chunkNumber ){
        set( "cn" , chunkNumber );
        set( "_ns" , "_chunks" );
    }
    
    protected abstract JSBinaryData getData();
    
    public Collection<String> keySet(){
        if ( get( "data") == null )
            set( "data" , getData() );
        return super.keySet();
    }
}
