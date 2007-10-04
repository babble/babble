// JSArray.java

package ed.js;

import java.util.*;

public class JSArray extends JSObject {

    public void setInt( int pos , Object v ){
        while ( _array.size() <= pos )
            _array.add( null );
        _array.set( pos , v );
        set( "length" , new Integer( _array.size() ) );
    }

    public Object getInt( int pos ){
        if ( pos >= _array.size() )
            return null;
        return _array.get( pos );
    }

    List<Object> _array = new ArrayList<Object>();
}
