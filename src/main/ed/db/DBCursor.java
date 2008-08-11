// DBCursor.java

package ed.db;

import java.util.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.lang.*;
import ed.util.*;

/**
 * @anonymous name : { forEach }, desc : { Executes a function for each of this cursor's elements. }, _name : {forEach}, alias : { DBCursor._staticMehods.forEach }, param : {title : (param), desc : (a function to execute on each iteration), type : (function), name : (f)}, return : { desc : (the array), type : (JSArray)}
 * @expose
 */
public class DBCursor extends JSObjectLame implements Iterator<JSObject> {

    /** Value is 1024*1024*25. */
    public static final long MAX_RAW_BYTES = 1024 * 1024 * 25;
    /** Value is 1024*1024*50. */
    public static final long MAX_OBJ_BYTES = 1024 * 1024 * 50;

    /** Initializes a new database cursor
     * @param collection collection to use
     * @param q query to perform
     * @param k keys to return from the query
     * @param cons the constructor for the collection
     */
    public DBCursor( DBCollection collection , JSObject q , JSObject k , JSFunction cons ){
        _collection = collection;
        _query = q;
        _keysWanted = k;
        _constructor = cons;
    }

    /** Types of cursors: iterator or array. */
    static enum CursorType { ITERATOR , ARRAY };

    public DBCursor copy(){
	DBCursor c = new DBCursor( _collection , _query , _keysWanted , _constructor );
	c._orderBy = _orderBy;
	c._numWanted = _numWanted;
	c._skip = _skip;
	return c;
    }

    // ---- querty modifiers --------

    /** Sorts this cursor's elements.
     * @param orderBy the fields on which to sort
     * @return a cursor pointing to the first element of the sorted results
     * @throws RuntimeException if the iterator exists
     */
    public DBCursor sort( JSObject orderBy ){
        if ( _it != null )
            throw new RuntimeException( "can't sort after executing query" );

        _orderBy = orderBy;
        return this;
    }

    /** Limits the number of elements returned.
     * @param n the number of elements to return
     * @return a cursor pointing to the first element of the limited results
     * @throws RuntimeException if the iterator exists
     */
    public DBCursor limit( int n ){
        if ( _it != null )
            throw new RuntimeException( "can't set limit after executing query" );

        _numWanted = n;
        return this;
    }

    /** Discards a given number of elements at the beginning of the cursor.
     * @param n the number of elements to skip
     * @return a cursor pointing to the new first element of the results
     * @throws RuntimeException if the iterator exists
     */
    public DBCursor skip( int n ){
        if ( _it != null )
            throw new RuntimeException( "can't set skip after executing query" );
        _skip = n;
        return this;
    }

    // ----  internal stuff ------

    private void _check(){
        if ( _it != null )
            return;

        if ( _collection != null && _query != null ){
            JSObject foo = _query;
            if ( _orderBy != null && _orderBy.keySet( false ).size() > 0 ){
                foo = new JSObjectBase();
                if ( _query != null ){
                    _query.set( Bytes.NO_REF_HACK , "z" );
                    foo.set( "query" , _query );
                }
                foo.set( "orderby" , _orderBy );
            }

            final long start = System.currentTimeMillis();
            _it = _collection.find( foo , _keysWanted , _skip , -1 * _numWanted );
            ProfilingTracker.tlGotTime( "db.queries." + _collection.getFullName() , System.currentTimeMillis() - start , foo );
        }

        if ( _it == null )
            _it = (new LinkedList<JSObject>()).iterator();
    }

    /** @unexpose */
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
        _collection.apply( _cur , false );
        _num++;

        if ( _keysWanted != null && _keysWanted.keySet( false ).size() > 0 && _cur instanceof JSObjectBase ){
            ((JSObjectBase)_cur).markAsPartialObject();
        }

        if ( _constructor != null &&
             _cur instanceof JSObjectBase ){

            JSObjectBase job = (JSObjectBase)_cur;

            if ( job.getConstructor() == null )
                job.setConstructor( _constructor , true );

        }

        if ( _cursorType == CursorType.ARRAY ){

            if ( _it instanceof DBApiLayer.Result ){
                final long tb = ((DBApiLayer.Result)_it).totalBytes();
                if ( tb >= MAX_RAW_BYTES ){
                    throw new RuntimeException( "database cursor in array mode and trying to store too much data" );
                }
            }

            _totalObjectSize += JSObjectSize.size( _cur );

            if ( _totalObjectSize > MAX_OBJ_BYTES )
                throw new RuntimeException( "database cursor in array mode and total object size is too big" );

            _nums.add( String.valueOf( _all.size() ) );
            _all.add( _cur );
        }

        if ( _cur instanceof JSObjectBase )
            ((JSObjectBase)_cur).markClean();

        return _cur;
    }

    private boolean _hasNext(){
        _check();

        if ( _numWanted > 0 && _num >= _numWanted )
            return false;

        return _it.hasNext();
    }

    /** @unexpose */
    public int numSeen(){
	return _num;
    }

    // ----- iterator api -----

    /** Checks if there is another element.
     * @return if there is another element
     */
    public boolean hasNext(){
        _checkType( CursorType.ITERATOR );
        return _hasNext();
    }

    /** Returns the element the cursor is at and moves the cursor ahead by one.
     * @return the next element
     */
    public JSObject next(){
        _checkType( CursorType.ITERATOR );
        return _next();
    }

    /** Returns the element the cursor is at.
     * @return the next element
     */
    public JSObject curr(){
        _checkType( CursorType.ITERATOR );
        return _cur;
    }

    /** Unimplemented.
     * @throws RuntimeException
     */
    public void remove(){
        throw new RuntimeException( "no" );
    }


    //  ---- array api  -----

    /** @unexpose */
    void _fill( int n ){
        _checkType( CursorType.ARRAY );
        while ( n >= _all.size() && _hasNext() )
            _next();
    }

    /** Gets the <tt>n</tt>th object from this results array.
     * @param n the index of the object to get
     * @return the object at the specified index
     */
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

    /** Gets the <tt>n</tt>th object from this results array.
     * @param n the index of the object to get
     * @return the object at the specified index
     */
    public Object getInt( int n ){
        _checkType( CursorType.ARRAY );
        _fill( n );
        return _all.get( n );
    }

    /** Finds the number of elements in the array.
     * @return the number of elements in the array
     */
    public int length(){
        _checkType( CursorType.ARRAY );
        _fill( Integer.MAX_VALUE );
        return _all.size();
    }

    /** Converts this cursor to an array.
     * @return an array of elements
     */
    public JSArray toArray(){
        return toArray( Integer.MAX_VALUE );
    }

    /** Converts this cursor to an array.  If there are more than a given number of elements in the resulting array, only return the first <tt>min</tt>.
     * @return min the minimum size of the array to return
     * @return an array of elements
     */
    public JSArray toArray( int min ){
        _checkType( CursorType.ARRAY );
        _fill( min );
        return new JSArray( _all );
    }

    /** Returns this array's keys.
     * @return a collection of keys
     */
    public Collection<String> keySet( boolean includePrototype ){
        _fill( Integer.MAX_VALUE );
        return _nums;
    }

    /** Counts the number of elements in this cursor.
     * @return the number of elements
     * @throws RuntimeException if there is no collection set
     * @throws RuntimeException if there is no _base property of the collection
     * @throws RuntimeException if there is no _collectionPrototype property of _base
     * @throws RuntimeException if this collection's prototype has no count function
     * @throws RuntimeException if the count function does not return a number
     */
    public int count(){
        if ( _collection == null )
            throw new RuntimeException( "why is _collection null" );
        if ( _collection._base == null )
            throw new RuntimeException( "why is _collection._base null" );
        if ( _collection._base._collectionPrototype == null )
            throw new RuntimeException( "why is _collection._base._collectionPrototype null" );

        JSFunction c = (JSFunction)(_collection._base._collectionPrototype.get( "count" ));
        if ( c == null )
            throw new RuntimeException( "can't find count function" );

        Scope s = c.getScope().child();
        s.setThis( this._collection );
        Object o = c.call( s , _query );
        if ( o instanceof Number )
            return ((Number)o).intValue();

        throw new RuntimeException( "why did call return a non-number" );
    }

    // ---- js methods ----
    /** @unexpose */
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

    // ----  query setup ----
    final DBCollection _collection;
    final JSObject _query;
    final JSObject _keysWanted;
    final JSFunction _constructor;
    
    private JSObject _orderBy;
    private int _numWanted = 0;
    private int _skip = 0;

    // ----  result info ----
    private Iterator<JSObject> _it;

    private CursorType _cursorType;
    private JSObject _cur = null;
    private int _num = 0;

    private long _totalObjectSize = 0;

    private final List<JSObject> _all = new ArrayList<JSObject>();
    private final List<String> _nums = new ArrayList<String>();

}
