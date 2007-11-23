// DBBase.java

package ed.db;

import java.util.*;

import ed.js.*;
import ed.js.engine.*;
import ed.js.func.*;

public abstract class DBBase extends JSObjectLame {

    public abstract DBCollection getCollectionFromFull( String fullNameSpace );
    public abstract DBCollection getCollection( String name );
    public abstract Collection<String> getCollectionNames();

    public Object get( Object n ){
        if ( n == null )
            return null;
        
        if ( n.toString().equals( "tojson" ) )
            return _tojson;

        if ( n instanceof String || 
             n instanceof JSString ){
            String s = n.toString();
            if ( s.startsWith( "." ) )
                return getCollectionFromFull( s.substring(1) );
            return getCollection( s );
        }

        return null;
    }

    public Collection<String> keySet(){
        return getCollectionNames();
    }

    class tojson extends JSFunctionCalls0{
        public Object call( Scope s , Object foo[] ){
            return toString();
        }
    }
    tojson _tojson = new tojson();
}
