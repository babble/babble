// JSObjectBase.java

package ed.js;

import java.util.*;

public class JSObjectBase implements JSObject {

    public JSObjectBase(){
    }

    public JSObjectBase( JSFunction constructor ){
        _constructor = constructor;
    }

    public Object set( Object n , Object v ){
        if ( n == null )
            throw new NullPointerException();
        
        if ( v != null && v instanceof String )
            v = new JSString( v.toString() );
        
        if ( n instanceof JSString )
            n = n.toString();
        
        if ( n instanceof String ){
            if ( _map == null )
                _map = new TreeMap<String,Object>();
            _map.put( (String)n , v );
            return v;
        }
        
        if ( n instanceof Number ){
            setInt( ((Number)n).intValue() , v );
            return v;
        }
        
        throw new RuntimeException( "what - " + n.getClass() );
    }

    public Object get( Object n ){
        if ( n == null )
            throw new NullPointerException();
        
        if ( n instanceof JSString )
            n = n.toString();
        
        if ( n instanceof String ){
            Object res = _map == null ? null : _map.get( ((String)n) );
            if ( res == null && _constructor != null )
                res = _constructor._prototype.get( n );
            return res;
        }
        
        if ( n instanceof Number )
            return getInt( ((Number)n).intValue() );
        

        throw new RuntimeException( "what - " + n.getClass() );
    }

    public Object setInt( int n , Object v ){
        return set( String.valueOf( n ) , v );
    }

    public Object getInt( int n ){
        return get( String.valueOf( n ) );
    }

    public Collection<String> keySet(){
        if ( _map == null )
            return EMPTY_SET;
        return _map.keySet();
    }

    public String toString(){
        return "Object";
    }

    private Map<String,Object> _map = null;// = new TreeMap<String,Object>();
    private JSFunction _constructor;

    static final Set<String> EMPTY_SET = Collections.unmodifiableSet( new HashSet<String>() );
}
