// DBBase.java

package ed.db;

import java.util.*;

import ed.js.*;

public abstract class DBBase extends JSObjectLame {

    public abstract DBCollection getCollection( String name );
    public abstract Collection<String> getCollectionNames();
    
    public Object get( Object n ){
        if ( n == null )
            return null;
        
        if ( n instanceof String || 
             n instanceof JSString )
            return getCollection( n.toString() );
        
        return null;
    }

    public Collection<String> keySet(){
        return getCollectionNames();
    }


}
