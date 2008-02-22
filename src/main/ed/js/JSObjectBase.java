// JSObjectBase.java

package ed.js;

import java.util.*;

import ed.db.*;
import ed.js.engine.*;

public class JSObjectBase implements JSObject {

    public JSObjectBase(){
    }

    public JSObjectBase( JSFunction constructor ){
        _constructor = constructor;
        if ( _constructor != null )
            set( "__proto__" , _constructor._prototype );
    }

    public void prefunc(){}

    public Object set( Object n , Object v ){
        _readOnlyCheck();
        prefunc();
        if ( n == null )
            n = "null";
        
        if ( v != null && v instanceof String )
            v = new JSString( v.toString() );
        
        if ( n instanceof JSString )
            n = n.toString();
        
        if ( v != null &&  "_id".equals( n ) &&
	     ( ( v instanceof String ) || ( v instanceof JSString ) )
	     ){
            v = new ObjectId( v.toString() );
        }
            
        
        if ( n instanceof String ){
            String name = (String)n;
            
            if ( _map == null ){
                _map = new TreeMap<String,Object>();
                _keys = new ArrayList<String>();
            }
            
            
            if ( ! ( name.equals( "__proto__" ) || name.equals( "__parent__" ) ) )
                if ( ! _map.containsKey( n ) )
                    _keys.add( name );
            
            _map.put( name , v );
            return v;
        }
        
        if ( n instanceof Number ){
            setInt( ((Number)n).intValue() , v );
            return v;
        }
        
        throw new RuntimeException( "object key can't be a [" + n.getClass() + "]" );
    }

    public Object get( Object n ){
        prefunc();
        if ( n == null )
            n = "null";
        
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
        
        throw new RuntimeException( "object key can't be a [" + n.getClass() + "]" );
    }

    public void removeField( Object n ){
        if ( n == null )
            return;
        

        if ( n instanceof JSString )
            n = n.toString();
        
        if ( n instanceof String ){
            _map.remove( (String)n );
            _keys.remove( n );
        }
        
    }


    public Object setInt( int n , Object v ){
        _readOnlyCheck();
        prefunc();
        return set( String.valueOf( n ) , v );
    }

    public Object getInt( int n ){
        prefunc();
        return get( String.valueOf( n ) );
    }


    public boolean containsKey( String s ){
        if ( _keys != null && _keys.contains( s ) )
            return true;
        return false;
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

    public void setConstructor( JSFunction cons , boolean exec ){
        _readOnlyCheck();
        
        _constructor = cons;
        if ( exec ){
            
            Scope s = _constructor.getScope();
            
            if ( s == null )
                s = Scope.GLOBAL;
            
            s = s.child();
            
            s.setThis( this );
            _constructor.call( s );
        }
    }

    public void setConstructor( JSFunction cons ){
        setConstructor( cons , false );
    }

    public JSFunction getConstructor(){
        return _constructor;
    }

    public void setReadOnly( boolean readOnly ){
        _readOnly = readOnly;
    }

    private final void _readOnlyCheck(){
        if ( _readOnly )
            throw new RuntimeException( "can't modify JSObject - read only" );
    }

    private Map<String,Object> _map = null;
    private List<String> _keys = null;
    private JSFunction _constructor;
    private boolean _readOnly = false;

    static final Set<String> EMPTY_SET = Collections.unmodifiableSet( new HashSet<String>() );
}
