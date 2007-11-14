// DBCursor.java

package ed.db;

import java.util.*;

import ed.js.*;
import ed.util.*;

public class DBCursor extends JSObjectLame implements Iterator<JSObject> {
    DBCursor( Iterator<JSObject> i ){
        _it = i;
    }

    public Collection<String> keySet(){
        _fill( 2 );
        return _nums;
    }

    public boolean hasNext(){
        return _it.hasNext();
    }

    public void remove(){
        throw new RuntimeException( "no" );
    }

    public JSObject next(){
        JSObject foo = _it.next();
        _nums.add( String.valueOf( _all.size() ) );
        _all.add( foo );
        return foo;
    }

    public Object get( Object n ){
        if ( n instanceof Number )
            return getInt( ((Number)n).intValue() );
        
        int i = StringParseUtil.parseInt( n.toString() , -1 );
        if ( i >=0 )
            return getInt( i );
        
        return null;
    }

    public Object getInt( int n ){
        _fill( n );
        return _all.get( n );
    }

    public int length(){
        _fill( Integer.MAX_VALUE );
        return _all.size();
    }
    
    public JSArray toArray(){
        return toArray( Integer.MAX_VALUE );
    }

    public JSArray toArray( int min ){
        _fill( min );
        return new JSArray( _all );
    }

    void _fill( int n ){
        while ( n >= _all.size() && hasNext() )
            next();
    }

    private final Iterator<JSObject> _it;
    private final List<JSObject> _all = new ArrayList<JSObject>();
    private final List<String> _nums = new ArrayList<String>();

}
