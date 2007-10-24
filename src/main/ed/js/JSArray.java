// JSArray.java

package ed.js;

import java.util.*;

public class JSArray extends JSObjectBase {
    
    public JSArray(){
        this( 0 );
    }

    public JSArray( int init ){
        _array = new ArrayList( Math.max( 16 , init ) );
        
        for ( int i=0; i<init; i++ )
            _array.add( null );
    }

    public JSArray( Object ... obj ){
        _array = new ArrayList( obj.length );
        for ( Object o : obj )
            _array.add( o );
    }

    public JSArray( List lst ){
        _array = lst;
    }

    public Object setInt( int pos , Object v ){
        while ( _array.size() <= pos )
            _array.add( null );
        _array.set( pos , v );
        return v;
    }

    public Object getInt( int pos ){
        if ( pos >= _array.size() ){
            return null;
        }
        return _array.get( pos );
    }

    public Object get( Object n ){
        if ( n != null )
            if ( n instanceof JSString || n instanceof String )
                if ( n.toString().equals( "length" ) )
                    return _array.size();
        
        int idx = _getInt( n );
        if ( idx >=0 )
            return getInt( idx );
        
        return super.get( n );
    }

    public Object set( Object n , Object v ){
        int idx = _getInt( n );
        if ( idx < 0 )
            return super.set( n , v );
        
        return setInt( idx , v );
    }
    
    public Collection<String> keySet(){
        Collection<String> p = super.keySet();
        
        List<String> keys = new ArrayList<String>( p.size() + _array.size() );
        
        for ( int i=0; i<_array.size(); i++ )
            keys.add( String.valueOf( i ) );

        keys.addAll( p );
        
        return keys;
    }

    public String toString(){
        StringBuilder buf = new StringBuilder();
        for ( int i=0; i<_array.size(); i++ ){
            if ( i > 0 )
                buf.append( "," );
            buf.append( _array.get( i ) );
        }
        return buf.toString();
    }

    int _getInt( Object o ){
        if ( o == null )
            return -1;

        if ( o instanceof JSString )
            o = o.toString();
        
        if ( ! ( o instanceof String ) )
            return -1;
        
        String str = o.toString();
        for ( int i=0; i<str.length(); i++ )
            if ( ! Character.isDigit( str.charAt( i ) ) )
                return -1;
        
        return Integer.parseInt( str );
    }

    final List<Object> _array;

}
