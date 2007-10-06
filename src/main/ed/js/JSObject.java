// JSObject.java

package ed.js;

import java.util.*;

public class JSObject {

    public void set( Object n , Object v ){
        if ( n == null )
            throw new NullPointerException();

        if ( n instanceof JSString )
            n = n.toString();
        
        if ( n instanceof String ){
            _map.put( (String)n , v );
            return;
        }
        
        if ( n instanceof Number ){
            setInt( ((Number)n).intValue() , v );
            return;
        }
        
        throw new RuntimeException( "what - " + n.getClass() );
    }

    public Object get( Object n ){
        final Object r = _get( n );
        return r;
    }

    Object _get( Object n ){
        if ( n == null )
            throw new NullPointerException();

        if ( n instanceof String )
            return _map.get( ((String)n) );
        
        if ( n instanceof Number )
            return getInt( ((Number)n).intValue() );
        
        if ( n instanceof JSString )
            return _map.get( n.toString() );

        throw new RuntimeException( "what - " + n.getClass() );
    }

    public void setInt( int n , Object v ){
        set( String.valueOf( n ) , v );
    }

    public Object getInt( int n ){
        return get( String.valueOf( n ) );
    }

    public Set<String> keySet(){
        return _map.keySet();
    }

    private Map<String,Object> _map = new TreeMap<String,Object>();
}
