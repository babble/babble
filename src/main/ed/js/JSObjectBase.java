// JSObjectBase.java

package ed.js;

import java.util.*;

import ed.db.*;

public class JSObjectBase implements JSObject {

    public JSObjectBase(){
    }

    public JSObjectBase( JSFunction constructor ){
        _constructor = constructor;
    }

    public void prefunc(){}

    public Object set( Object n , Object v ){
        prefunc();
        if ( n == null )
            throw new NullPointerException();
        
        if ( v != null && v instanceof String )
            v = new JSString( v.toString() );
        
        if ( n instanceof JSString )
            n = n.toString();
        
        if ( v != null &&  "_id".equals( n ) && ! ( v instanceof ObjectId ) ){
            v = new ObjectId( v.toString() );
        }
            

        if ( n instanceof String ){
            if ( _map == null ){
                _map = new TreeMap<String,Object>();
                _keys = new ArrayList<String>();
            }
            
            if ( ! _map.containsKey( n ) )
                _keys.add( (String)n );
            
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
        prefunc();
        if ( n == null )
            throw new NullPointerException();
        
        if ( n instanceof JSString )
            n = n.toString();
        
        if ( n instanceof String ){
            Object res = _map == null ? null : _map.get( ((String)n) );
            if ( res == null && _constructor != null ){
                res = _constructor._prototype.get( n );
            }
            return res;
        }
        
        if ( n instanceof Number )
            return getInt( ((Number)n).intValue() );
        

        throw new RuntimeException( "what - " + n.getClass() );
    }

    public Object setInt( int n , Object v ){
        prefunc();
        return set( String.valueOf( n ) , v );
    }

    public Object getInt( int n ){
        prefunc();
        return get( String.valueOf( n ) );
    }

    public Collection<String> keySet(){
        prefunc();
        if ( _keys == null )
            return EMPTY_SET;
        return _keys;
    }

    public String toString(){
        return "Object";
    }

    protected void addAll( JSObject other ){
        for ( String s : other.keySet() )
            set( s , other.get( s ) );
    }

    public String getJavaString( Object name ){
        Object foo = get( name );
        if ( foo == null )
            return null;
        return foo.toString();
    }

    public void setConstructor( JSFunction cons ){
        _constructor = cons;
    }

    private Map<String,Object> _map = null;
    private List<String> _keys = null;
    private JSFunction _constructor;

    static final Set<String> EMPTY_SET = Collections.unmodifiableSet( new HashSet<String>() );
}
