// JSPyObjectWrapper.java

package ed.lang.python;

import java.util.*;

import org.python.core.*;

import ed.js.*;
import static ed.lang.python.Python.*;

public class JSPyObjectWrapper implements JSObject {

    public JSPyObjectWrapper( PyObject o ){
        _p = o;
        if ( _p == null )
            throw new NullPointerException( "don't think you should create a JSPyObjectWrapper for null" );
    }
    
    public Object set( Object n , Object v ){
        _p.__setitem__( toPython( n ) , toPython( v ) );
        return v;
    }

    public Object get( Object n ){
        PyObject p = toPython( n );
        
        Object o = _p.__finditem__( p );
        if ( o == null )
            o = _p.__findattr__( n.toString() );
        
        return toJS( o );
    }
    
    public Object setInt( int n , Object v ){
        throw new RuntimeException( "not implemented" );
    }
    public Object getInt( int n ){
        throw new RuntimeException( "not implemented" );
    }
    
    public Object removeField( Object n ){
        throw new RuntimeException( "not implemented" );
    }
    
    public boolean containsKey( String s ){
        // TODO: make less awful
        return keySet().contains( s );
    }
    

    public Collection<String> keySet(){
        
        List<String> keys = new ArrayList<String>();
    
        if ( _p instanceof PyDictionary ){
            for ( Object o : ((PyDictionary)_p).keySet() )
                keys.add( o.toString() );
        }
        else {
            for ( PyObject o : _p.asIterable() ){
                keys.add( o.toString() );
            }
            System.out.println( "H1 keys : " + keys );
        }
        
        return keys;
    }
    
    public JSFunction getConstructor(){
        throw new RuntimeException( "not implemented" );
    }

    public JSObject getSuper(){
        throw new RuntimeException( "not implemented" );
    }
    
    public String toString(){
        return _p.toString();
    }
    
    final PyObject _p;
}
    
