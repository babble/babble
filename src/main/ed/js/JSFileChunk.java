// JSFileChunk.java

package ed.js;

import java.util.*;

public class JSFileChunk extends JSObjectBase {

    protected JSFileChunk( JSFile f , int chunkNumber ){
        this();
        set( "cn" , chunkNumber );
    }

    public JSFileChunk(){
        set( "_ns" , "_chunks" );
    }
    
    protected JSBinaryData getData(){
        if ( get( "data" ) != null )
            return (JSBinaryData)get("data");
        
        throw new RuntimeException( "don't have any data :(" );
    }
    
    public JSFileChunk getNext(){
        JSObject n = (JSObject)get( "next" );
        if ( n == null )
            return null;
        n.keySet();

        return (JSFileChunk)(get( "next" ));
    }
    
    public Collection<String> keySet(){
        if ( get( "cn" ) == null )
            throw new RuntimeException( "bad chunk" );

        if ( get( "data") == null )
            set( "data" , getData() );
        return super.keySet();
    }

    
}
