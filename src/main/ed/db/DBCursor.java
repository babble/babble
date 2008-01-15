// DBCursor.java

package ed.db;

import java.util.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.util.*;

public class DBCursor extends JSObjectLame implements Iterator<JSObject> {


    DBCursor( DBCollection collection , JSObject q , JSObject k , JSFunction cons ){
        _collection = collection;
        _query = q;
        _keysWanted = k;
        _constructor = cons;
    }
    
    static enum CursorType { ITERATOR , ARRAY };

    // ---- querty modifiers --------
    
    public DBCursor sort( JSObject orderBy ){
        if ( _it != null )
            throw new RuntimeException( "can't sort after executing query" );
        
        _orderBy = orderBy;
        return this;
    }

    public DBCursor limit( int n ){
        if ( _it != null )
            throw new RuntimeException( "can't set limit after executing query" );

        _numWanted = n;
        return this;
    }
    
    // ----  internal stuff ------
    
    private void _check(){
        if ( _it != null )
            return;
            
        if ( _collection != null && _query != null ){
            JSObject foo = _query;
            if ( _orderBy != null && _orderBy.keySet().size() > 0 ){
                foo = new JSObjectBase();
                if ( _query != null )
                    foo.set( "query" , _query );
                foo.set( "orderby" , _orderBy );
            }
            _it = _collection.find( foo , _keysWanted , _numWanted );
        }

        if ( _it == null )
            _it = (new LinkedList<JSObject>()).iterator();
    }

    void _checkType( CursorType type ){
        if ( _cursorType == null ){
            _cursorType = type;
            return;
        }

        if ( type == _cursorType )
            return;
        
        throw new RuntimeException( "can't switch cursor access methods" );
    }

    private JSObject _next(){
        if ( _cursorType == null )
            _checkType( CursorType.ITERATOR );

        _check();
        
        _cur = null;
        _cur = _it.next();
        _num++;
        
        if ( _constructor != null && _cur instanceof JSObjectBase )
            ((JSObjectBase)_cur).setConstructor( _constructor );
        
        if ( _cursorType == CursorType.ARRAY ){
            _nums.add( String.valueOf( _all.size() ) );
            _all.add( _cur );
        }
        return _cur;
    }

    private boolean _hasNext(){
        _check();
        
        if ( _numWanted > 0 && _num >= _numWanted )
            return false;

        return _it.hasNext();
    }


    // ----- iterator api -----

    public boolean hasNext(){
        _checkType( CursorType.ITERATOR );
        return _hasNext();
    }

    public JSObject next(){
        _checkType( CursorType.ITERATOR );
        return _next();
    }

    public JSObject curr(){
        _checkType( CursorType.ITERATOR );
        return _cur;
    }

    public void remove(){
        throw new RuntimeException( "no" );
    }

    //  ---- array api  -----

    void _fill( int n ){
        while ( n >= _all.size() && _hasNext() )
            _next();
    }
    
    public Object get( Object n ){

        if ( n == null )
            return null;
        
        if ( n instanceof Number )
            return getInt( ((Number)n).intValue() );
        
        int i = StringParseUtil.parseInt( n.toString() , -1 );
        if ( i >= 0 )
            return getInt( i );
        
        return _staticMehods.get( n.toString() );
    }

    public Object getInt( int n ){
        _checkType( CursorType.ARRAY );
        _fill( n );
        return _all.get( n );
    }

    public int length(){
        _checkType( CursorType.ARRAY );
        _fill( Integer.MAX_VALUE );
        return _all.size();
    }
    
    public JSArray toArray(){
        return toArray( Integer.MAX_VALUE );
    }

    public JSArray toArray( int min ){
        _checkType( CursorType.ARRAY );
        _fill( min );
        return new JSArray( _all );
    }

    public Collection<String> keySet(){
        _fill( 2 );
        return _nums;
    }

    // ---- js methods ----
    private static Map<String,JSFunction> _staticMehods = new TreeMap<String,JSFunction>();
    static {
        _staticMehods.put( "forEach" , new JSFunctionCalls1(){
                public Object call( Scope s , Object fo , Object foo[] ){
                    DBCursor c = (DBCursor)(s.getThis());
                    JSFunction f = (JSFunction)fo;
                    
                    while ( c.hasNext() ){
                        f.call( s , c.next() );
                    }
                    
                    return null;
                }
            } );
        

    }
    
    // ----  members ----

    private Iterator<JSObject> _it;

    private DBCollection _collection;
    private JSObject _query;
    private JSObject _keysWanted;
    private JSObject _orderBy;
    private JSFunction _constructor;
    private int _numWanted = 0;

    private CursorType _cursorType;
    
    private JSObject _cur = null;
    private int _num = 0;

    private final List<JSObject> _all = new ArrayList<JSObject>();
    private final List<String> _nums = new ArrayList<String>();

}
