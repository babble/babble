// JSArray.java

package ed.js;

import java.util.*;

public class JSArray extends JSObject {
    
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

    public void setInt( int pos , Object v ){
        while ( _array.size() <= pos )
            _array.add( null );
        _array.set( pos , v );
        set( "length" , new Integer( _array.size() ) );
    }

    public Object getInt( int pos ){
        if ( pos >= _array.size() ){
            return null;
        }
        return _array.get( pos );
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

    final List<Object> _array;
}
