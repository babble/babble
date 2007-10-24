// DBCollection.java

package ed.db;

import java.util.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;

public abstract class DBCollection extends JSObjectLame {
    
    public abstract JSObject save( JSObject o );
    public abstract ObjectId apply( JSObject o );
    public abstract JSObject find( ObjectId id );

    protected DBCollection( String name ){
        _name = name;

        _entries.put( "name" , _name );

        _entries.put( "save" , new JSFunctionCalls1() {
                public Object call( Scope s , Object o , Object foo[] ){
                    if ( ! ( o instanceof JSObject ) )
                        throw new RuntimeException( "can't only save JSObject" );
                    return save( (JSObject)o );
                }
            } );

        _entries.put( "apply" , new JSFunctionCalls1() {
                public Object call( Scope s , Object o , Object foo[] ){
                    if ( ! ( o instanceof JSObject ) )
                        throw new RuntimeException( "can't only apply JSObject" );
                    return apply( (JSObject)o );
                }
            } );

        _entries.put( "find" , new JSFunctionCalls1() {
                public Object call( Scope s , Object o , Object foo[] ){

                    if ( o instanceof ObjectId )
                        return find( (ObjectId)o );
                    
                    throw new RuntimeException( "wtf " );
                }
            } );

    }

    public Object get( Object n ){
        return _entries.get( n );
    }

    protected Map _entries = new TreeMap();
    final String _name;
}
