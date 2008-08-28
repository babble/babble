// JSObjectBase.java

/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package ed.js;

import java.io.*;
import java.util.*;

import ed.db.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.util.*;

/** @expose */
public class JSObjectBase implements JSObject {

    /** Prefix for getters and setters: <tt>_____gs____</tt> (asymmetric) */
    public static final String GETSET_PREFIX = "_____gs____";
    /** Prefix for scope failovers: <tt>_____scope_failover____</tt> (asymmetric) */
    public static final String SCOPE_FAILOVER_PREFIX = "_____scope_failover____";

    /** Reserved key names: <tt>__proto__</tt>, <tt>__constructor__</tt>, <tt>constructor</tt>, and <tt>__parent____</tt> */
    static final Set<String> BAD_KEY_NAMES = new HashSet<String>();
    static {
        BAD_KEY_NAMES.add( "__proto__" );
        BAD_KEY_NAMES.add( "__constructor__" );
        BAD_KEY_NAMES.add( "constructor" );
        BAD_KEY_NAMES.add( "__parent____" );
        BAD_KEY_NAMES.add( "_dontEnum" );
        
        JS._debugSIStart( "JSObjectBase" );
    }

    /** The name for objects: "Object" */
    static final String OBJECT_STRING = "Object";

    /** Initialize a new object */
    public JSObjectBase(){
    }

    /** Initialize a new object and set its constructor
     * @param constructor The constructor for this object to use
     */
    public JSObjectBase( JSFunction constructor ){
        setConstructor( constructor );
    }

    /** Sets up any necessary fields for this object. */
    public void prefunc(){}

    /** Sets or creates this object's field with the key <tt>n</tt> to the value <tt>v</tt>
     * @param n Key to set
     * @param v Value to set
     * @return v
     * @throws {RuntimeException} If <tt>n</tt> starts with <tt>GETSET_PREFIX</tt> and does not end with <tt>GET</tt> or <tt>SET</tt>.
     */
    public Object set( Object n , Object v ){
        _readOnlyCheck();
        prefunc();

        _dirty = _dirty || ! ByteEncoder.dbOnlyField( n );

        if ( n == null )
            n = "null";

        if ( v != null && "_id".equals( n ) &&
	     ( ( v instanceof String ) || ( v instanceof JSString ) )
	     ){
            v = new ObjectId( v.toString() );
        }

        if ( v != null && v instanceof String )
            v = new JSString( v.toString() );

        if ( n instanceof Number ){
            setInt( ((Number)n).intValue() , v );
            return v;
        }

        String name = n.toString();

        if ( name.startsWith( GETSET_PREFIX ) ){
            name = name.substring( GETSET_PREFIX.length() );
            String type = name.substring( 0 , 3 );
            name = name.substring( 3 );
            if ( type.equals( "GET" ) )
                setGetter( name , (JSFunction)v );
            else if ( type.equals( "SET" ) )
                setSetter( name , (JSFunction)v );
            else
                throw new RuntimeException( "broken" );
            return v;
        }

	// SPECIAL

	if ( name.equals( "constructor" ) || name.equals( "__constructor__" ) ){
	    _constructor = (JSFunction)v;
	    _dirtyMyself();
	    return _constructor;
	}

	if( name.equals( "__proto__" ) ){
	    __proto__ = (JSObject)v;
	    _dirtyMyself();
	    return __proto__;
	}

	// END SPECIAL

        JSFunction func = getSetter( name );
        if ( func != null )
            return _call( func , v );

        _checkMap();

        if ( ! BAD_KEY_NAMES.contains( name ) )
            if ( ! _map.containsKey( name ) )
                _keys.add( name );

        _dirtyMyself();
        _map.put( name , v );
        if ( v instanceof JSObjectBase )
            ((JSObjectBase)v)._name = name;
        return v;
    }

    private void _checkMap(){
        if ( _map == null ){
            //_map = new HashMap<String,Object>();
            _map = new FastStringMap();
        }

        if ( _keys == null )
            _keys = new ArrayList<String>();

        _dirtyMyself();
    }

    /** Given a key for this object, return its corresponding value.
     * @param n Key for which to look.
     * @return v The corresponding value as a string.
     */
    public String getAsString( Object n ){
        final Object v = get( n );
        if ( v == null )
            return null;

        return v.toString();
    }

    /** Given a key for this object, return its corresponding value.
     * @param n Key for which to look.
     * @return v The corresponding value.
     */
    public Object get( Object n ){

        prefunc();

        if ( n == null )
            n = "null";

        if ( n instanceof Number )
            return getInt( ((Number)n).intValue() );

        return _simpleGet( n.toString() );
    }

    /** @unexpose */
    public Object _simpleGet( String s ){

        final boolean scopeFailover = s.startsWith( SCOPE_FAILOVER_PREFIX );
        if ( scopeFailover )
            s = s.substring( SCOPE_FAILOVER_PREFIX.length() );
        
        return _simpleGet( s.hashCode() , s , 0 , null , scopeFailover , BAD_KEY_NAMES.contains( s ) );
    }

    /** @unexpose */
    Object _simpleGet( final int hash , final String s , int depth , IdentitySet<JSObject> seen , final boolean scopeFailover , final boolean badKey ){
        if ( depth > 100 ) // safety
            return null;
        
        if ( depth > 5 ){
            if ( seen == null ){
                seen = new IdentitySet<JSObject>();
            }
            else {
                if ( seen.contains( this ) )
                    return null;
                seen.add( this );
            }
        }
        
	// SPECIAL
        if ( depth == 0 ){
            if ( s.equals( "constructor" ) || s.equals( "__constructor__" ) )
                return _constructor;
            
            if ( s.equals( "__proto__" ) )
                return __proto__;
        }
        
	// END SPECIAL


        Object res = null;

        if ( depth == 0 && ! badKey ){
            JSFunction f = getGetter( s );
            if ( f != null )
                return _call( f );
        }

        if ( _map != null ){
            res = _mapGet( hash , s );
            if ( res != null || _map.containsKey( hash , s ) ) return res;
        }

        res = _getFromParent( hash , s , depth , seen , scopeFailover , badKey );
        if ( res != null ) return res;

        if ( _objectLowFunctions != null
             && _constructor == null ){
            res = _objectLowFunctions.get( s );
            if ( res != null ) return res;
        }

        if ( depth == 0 &&
             ! "__notFoundHandler".equals( s ) &&
             ! scopeFailover &&
             ! badKey
             ){
            
            JSFunction f = _getNotFoundHandler();
            if ( f != null ){
                Scope scope = f.getScope();
                if ( scope == null ){
                    scope = Scope.getAScope( false );
                    if ( scope == null )
                        throw new RuntimeException( "not found handler doesn't have a scope and no thread local scope" );
                }

                scope = scope.child();
                scope.setThis( this );
                if ( ! _inNotFoundHandler.get() ){
                    try {
                        _inNotFoundHandler.set( true );
                        return f.call( scope , s );
                    }
                    finally {
                        _inNotFoundHandler.set( false );
                    }
                }
            }
        }

        if ( scopeFailover ){
            Scope scope = Scope.getAScope( false );
            if ( scope != null )
                return scope.get( s );
        }

        return null;
    }

    private JSFunction _getNotFoundHandler(){
        Object blah = _simpleGet( "__notFoundHandler" );
        if ( blah instanceof JSFunction )
            return (JSFunction)blah;

        return null;
    }


    // ----
    // inheritnace jit START
    // ----

    private Object _getFromParent( final int hash , final String s , final int depth , final IdentitySet<JSObject> seen  , final boolean scopeFailover , final boolean badKey ){
        _getFromParentCalls++;

        if ( s.equals( "__proto__" ) || s.equals( "prototype" ) )
            return null;

        boolean jit = false;

        if ( ( depth > 0 && _getFromParentCalls > 50 ) ||
             _getFromParentCalls > 1000 ){
            if ( _dependenciesOk() ){

                jit = true;

                if ( _jitCache == null )
                    _jitCache = new HashMap<String,Object>();

                if ( _jitCache.containsKey( s ) ){
                    return _jitCache.get( s );
                }
            }
            else {
                _dependencies();
            }
        }

        final Object res = _getFromParentHelper( hash , s , depth , seen , scopeFailover , badKey );
        if ( jit )
            _jitCache.put( s , res );

        return res;
    }

    private Object _getFromParentHelper( final int hash , final String s , final int depth , final IdentitySet<JSObject> seen , final boolean scopeFailover , final boolean badKey){

        _updatePlacesToLook();

        final int max = _placesToLook.length;

        for ( int i=0; i<max; i++ ){
            JSObject o = _placesToLook[i];
            if ( o == null || o == this )
                continue;

            if ( o instanceof JSObjectBase ){
                JSObjectBase job = (JSObjectBase)o;
                Object res = job._simpleGet( hash , s , depth + 1 , seen , scopeFailover , badKey );
                if ( res != null )
                    return res;

                continue;
            }

            Object res = o.get( s );
            if ( res != null )
                return res;
        }

        return null;
    }

    private void _updatePlacesToLook(){

        if ( _placesToLookUpdated )
            return;

	_placesToLook[0] = __proto__;
	_placesToLook[1] = (JSObject)_mapGet( "prototype" );

        if ( _constructor != null ){
            _placesToLook[2] = _constructor._prototype;
            _placesToLook[3] = _constructor;
        }
	else {
	    _placesToLook[2] = null;
	    _placesToLook[3] = null;
	}

        for ( int i=1; i<_placesToLook.length; i++ )
            for ( int j=0; j<i; j++ )
                if ( _placesToLook[i] == _placesToLook[j] )
                    _placesToLook[i] = null;

        _placesToLookUpdated = true;
    }

    private boolean _dependenciesOk(){
        if ( _badDepencies )
            return false;

        if ( _dependencies == null )
            return false;

        List<JSObjectBase> lst = _dependencies;
        return sum(lst) == _dependencySum;
    }

    private List<JSObjectBase> _dependencies(){
        if ( _badDepencies )
            return null;

        if ( _dependenciesOk() )
            return _dependencies;

        List<JSObjectBase> lst = new ArrayList<JSObjectBase>();
        lst = _addDependencies( lst );

        if ( lst == null )
            _badDepencies = true;
        
        _dependencies = lst;
        _dependencySum = sum( lst );
        
        return lst;
    }

    private static int sum( List<JSObjectBase> lst ){
        int sum = 0;
        for ( int i=0; i<lst.size(); i++)
            sum += lst.get(i)._version;
        return sum;
    }

    /** @unexpose */
    protected List<JSObjectBase> _addDependencies( List<JSObjectBase> lst ){
        _updatePlacesToLook();
	all:
        for ( int i=0; i<_placesToLook.length; i++ ){

            JSObjectBase job = (JSObjectBase)_placesToLook[i];
            if ( job == null || job == this )
                continue;

            // uh-oh
            if ( ! ( job instanceof JSObjectBase ) )
                return null;
	    
	    for ( int j=0; j<lst.size(); j++ )
		if ( lst.get( j ) == job )
		    continue all;

            lst.add( job );
            job._addDependencies( lst  );
        }
        return lst;
    }

    // ----
    // inheritnace jit END
    // ----

    /** Given a field name in this object, remove it.
     * @param n The name of the field to be removed.
     * @return The removed value, if successful, otherwise null.
     */
    public Object removeField( Object n ){
        if ( n == null )
            return null;

        if ( n instanceof JSString )
            n = n.toString();

        Object val = null;

        if ( n instanceof String ){
            if ( _map != null )
                val = _map.remove( (String)n );
            if ( _keys != null )
                _keys.remove( n );
        }

        return val;
    }

    /** Add a key/value pair to this object, using a numeric key.
     * @param n Key to use.
     * @param v Value to use.
     * @param v
     */
    public Object setInt( int n , Object v ){
        _readOnlyCheck();
        prefunc();
        return set( String.valueOf( n ) , v );
    }

    /** Get a value from this object whose key is numeric.
     * @param n Key for which to search.
     * @return The corresponding value.
     */
    public Object getInt( int n ){
        prefunc();
        return get( String.valueOf( n ) );
    }

    /** Tests if the specified string is a key in this object.
     * @param s String to test.
     * @return If the string is a key.
     */
    public boolean containsKey( String s ){
        return containsKey( s , true );
    }

    public boolean containsKey( final String s , final boolean includePrototype){
        return containsKey( s.hashCode() , s , includePrototype );
    }

    public boolean containsKey( final int hash , final String s , final boolean includePrototype){
        prefunc();
        
        if ( _map != null && _map.containsKey( hash , s ) )
            return true;

        if ( includePrototype == false || _constructor == null ) return false;

        IdentitySet<JSObjectBase> seen = new IdentitySet<JSObjectBase>();

        JSObjectBase start = _constructor._prototype;

        while ( start != null ){
            if ( seen.contains( start ) )
                break;

            if( start.containsKey( hash , s , false ) )
                return true;

            seen.add( start );

            if ( start._constructor == null )
                break;
            start = start._constructor._prototype;
        }

        return false;
    }

    public boolean hasOwnProperty( String s ){
        prefunc();
        return _map != null && _map.containsKey( s );
    }
    
    /** Returns a collection of all the keys for this object.
     * @return The keys for this object.
     */
    public final Collection<String> keySet(){
        return keySet( true );
    }
    
    public Collection<String> keySet( boolean includePrototype ){
        prefunc();

	List<String> keys = new ArrayList<String>();

        if ( _keys != null )
	    keys.addAll( _keys );
        
        if ( includePrototype && _constructor != null ){

            IdentitySet<JSObjectBase> seen = new IdentitySet<JSObjectBase>();
            JSObjectBase start = _constructor._prototype;

            while ( start != null && ! JSInternalFunctions.JS_evalToBool( start.get( "_dontEnum" ) ) ){
                if ( seen.contains( start ) )
                    break;

                keys.addAll( start.keySet( false ) );
                seen.add( start );
                
                if ( start._constructor == null )
                    break;
                start = start._constructor._prototype;
            }
        }

	if ( _dontEnum != null )
	    keys.removeAll( _dontEnum );
	
	return keys;
    }

    public void dontEnumExisting(){
	if ( _dontEnum == null )
	    _dontEnum = new HashSet();
        _dontEnum.addAll( keySet() );
    }

    public void dontEnum( String s ){
	if ( _dontEnum == null )
	    _dontEnum = new HashSet();
	_dontEnum.add( s );
    }

    // ----
    // [gs]etter
    // ---

    /** Set this object's setter.
     * @param name Identifier for the getter/setter pair
     * @param func Function to which to set setter
     */
    void setSetter( String name , JSFunction func ){
        _dirtyMyself();
        _getSetterAndGetter( name , true ).second = func;
    }

    /** Set this object's getter.
     * @param name Identifier for the getter/setter pair
     * @param func Function to which to set getter
     */
    void setGetter( String name , JSFunction func ){
        _dirtyMyself();
        _getSetterAndGetter( name , true ).first = func;
    }

    /** Get this object's setter.
     * @param name Identifier for the getter/setter pair
     */
    JSFunction getSetter( String name ){
        Pair<JSFunction,JSFunction> p = _getSetterAndGetter( name, false );
        if ( p != null )
            return p.second;

        JSObject s = getSuper();
        if ( s != null && s != this )
            return ((JSObjectBase)s).getSetter( name );

        return null;
    }

    /** Get this object's getter.
     * @param name Identifier for the getter/setter pair
     */
    JSFunction getGetter( String name ){
        Pair<JSFunction,JSFunction> p = _getSetterAndGetter( name, false );
        if ( p != null )
            return p.first;

        JSObject s = getSuper();
        if ( s instanceof JSObjectBase && s != this )
            return ((JSObjectBase)s).getGetter( name );

        return null;
    }

    /** Get a string representation of the setter's name
     * @param name Getter/setter identifier
     * @return GETSET_PREFIX+"SET"+name
     */
    public static String setterName( String name ){
        return GETSET_PREFIX + "SET" + name;
    }

    /** Get a string representation of the getter's name
     * @param name Getter/setter identifier
     * @return GETSET_PREFIX+"GET"+name
     */
    public static String getterName( String name ){
        return GETSET_PREFIX + "GET" + name;
    }

    // ---

    /**
     * @return a nice String representation.  user defined if available
     *         or json
     */
    public String toPrettyString(){
        Object temp = get( "toString" );
        
        if ( ! ( temp instanceof JSFunction ) )
            return JSON.serialize( this );
        
        return toString();
    }
    
    /** Get the string representation of this object.
     * @return the string representation of this object.
     */
    public String toString(){
        Object temp = get( "toString" );

        if ( ! ( temp instanceof JSFunction ) )
            return OBJECT_STRING;

        JSFunction f = (JSFunction)temp;

        Scope s;
        try {
            s= f.getScope().child();
            s.setThis( this );
        } catch(RuntimeException t) {
            throw t;
        }

        Object res = f.call( s );
        if ( res == null )
            return "Object(toString was null)";
        return res.toString();
    }

    /** Given an object, add all of its key/value pairs to this object.
     * @param other The source object
     */
    protected void addAll( JSObject other ){
        for ( String s : other.keySet() )
            set( s , other.get( s ) );
    }

    private Object _call( JSFunction func , Object ... params ){
        Scope sc = Scope.getAScope();
        sc.setThis( this );
        try {
            return func.call( sc , params );
        }
        finally {
            sc.clearThisNormal( null );
        }
    }

    /** Gets a given field in this object and returns it as a string.
     * @param name Name of the field to find.
     * @return The field as a string.
     */
    public String getJavaString( Object name ){
        Object foo = get( name );
        if ( foo == null )
            return null;
        return foo.toString();
    }

    /** Set a constructor for this object.
     * @param cons Function to be this object's constructor.
     */
    public void setConstructor( JSFunction cons ){
        setConstructor( cons , false , null );
    }

    /** Set a constructor for this object and choose whether or not to execute it.
     * @param cons Function to be this object's constructor.
     * @param exec If the constructor should be executed now.
     */
    public void setConstructor( JSFunction cons , boolean exec ){
        setConstructor( cons , exec , null );
    }

    /** Set a constructor for this object, choose whether or not to execute it, and if so pass it arguments.
     * @param cons Function to be this object's constructor.
     * @param exec If the constructor should be executed now.
     * @param args Arguments to pass to the constructor if it is to be executed now.
     */
    public void setConstructor( JSFunction cons , boolean exec , Object args[] ){
        _readOnlyCheck();
        _dirtyMyself();

        _constructor = cons;
        //_mapSet( "__constructor__" , _constructor );
        //_mapSet( "constructor" , _constructor );

	__proto__ = _constructor == null ? null : _constructor._prototype;
        //_mapSet( "__proto__" , __proto__ );

        if ( _constructor != null && exec ){

            Scope s = _constructor.getScope();

            if ( s == null )
                s = Scope.getThreadLocal();

            s = s.child();

            s.setThis( this );
            _constructor.call( s , args );
        }
    }

    /** Returns this object's constructor.
     * @return The constructor.
     */
    public JSFunction getConstructor(){
        return _constructor;
    }

    /** Get the prototype for this object.
     * @return The prototype or constructor's prototype, if found, otherwise null.
     */
    public JSObject getSuper(){

	if ( __proto__ != null )
	    return __proto__;

        if ( _constructor != null && _constructor._prototype != null )
            return _constructor._prototype;


        return null;
    }

    /** Lock this object to prevent setting fields. Makes the object immutable. */
    public void lock(){
        setReadOnly( true );
    }

    /** Sets if an object is locked or not.
     * @param readOnly If this object's fields should be read-only.
     */
    public void setReadOnly( boolean readOnly ){
        _readOnly = readOnly;
    }

    private final void _readOnlyCheck(){
        if ( _readOnly )
            throw new RuntimeException( "can't modify JSObject - read only" );
    }

    /** Given an object, add all of its key/value pairs to this object.
     * @param other The source object
     */
    public void extend( JSObject other ){
        if ( other == null )
            return;

        for ( String key : other.keySet() ){
            set( key , other.get( key ) );
        }

    }

    /** @unexpose */
    public void debug(){
        try {
            debug( 0 , System.out );
        }
        catch ( IOException ioe ){
            ioe.printStackTrace();
        }
    }

    /** @unexpose */
    Appendable _space( int level , Appendable a )
        throws IOException {
        for ( int i=0; i<level; i++ )
            a.append( "  " );
        return a;
    }

    /** @unexpose */
    public void debug( int level , Appendable a )
        throws IOException {
        _space( level , a );

        a.append( "me :" );
        if ( _name != null )
            a.append( " name : [" ).append( _name ).append( "] " );
        if( _keys != null )
            a.append( "keys : " ).append( _keys.toString() );
        a.append( " " + System.identityHashCode( this ) );
        a.append( "\n" );

        if ( _map != null ){
            JSObjectBase p = (JSObjectBase)_simpleGet( "prototype" );
            if ( p != null ){
                _space( level + 1 , a ).append( "prototype || " + System.identityHashCode( p ) + "\n" );
                p.debug( level + 2 , a );
            }

        }

        if ( _constructor != null ){
            _space( level + 1 , a ).append( "__constructor__ ||\n" );
            _constructor.debug( level + 2 , a );
        }
    }

    /** The hash code value of this object.
     * @return The hash code value of this object.
     */
    public final int hashCode(){
        return hashCode( null );
    }

    protected int hashCode( IdentitySet seen ){

        int hash = 81623;

        if ( _constructor != null )
            hash += _constructor.hashCode();
        
        if ( _map == null )
            return hash;

        if ( seen == null ){
            seen = new IdentitySet();
            seen.add( this );
        }
        
        for ( Map.Entry<String,Object> e : _map.entrySet() ){
            final String key = e.getKey();

            if ( JSON.IGNORE_NAMES.contains( key ) )
                continue;
            
            hash += ( 3 * key.hashCode() );

            final Object value = e.getValue();
            hash += _hash( seen , value );
            
        }

        return hash;
    }

    protected int _hash( IdentitySet seen , Object value ){
        if ( value == null )
            return 0;
        
        if ( seen != null ){
            if ( seen.contains( value ) )
                return 0;
            seen.add( value );
        }
        
        if ( value instanceof JSObjectBase )
            return ( 7 * ((JSObjectBase)value).hashCode( seen ) );

        return 7 * value.hashCode();
        
    }

    // -----
    // name is very weird. it probably doesn't work the way you think or want
    // ----

    /** @unexpose */
    public String _getName(){
        return _name;
    }

    /** @unexpose */
    public void _setName( String n ){
        _name = n;
    }

    private Pair<JSFunction,JSFunction> _getSetterAndGetter( final String name , final boolean add ){

        if ( _setterAndGetters == null && ! add )
            return null;

        if ( _setterAndGetters == null ){
            Map<String,Pair<JSFunction,JSFunction>> m = new TreeMap<String,Pair<JSFunction,JSFunction>>();
            synchronized ( _setterAndGettersSetLOCK ){
                if ( _setterAndGetters == null )
                    _setterAndGetters = m;
            }
        }

        synchronized ( _setterAndGetters ){

            Pair<JSFunction,JSFunction> p = _setterAndGetters.get( name );
            if ( ! add || p != null )
                return p;

            p = new Pair<JSFunction,JSFunction>();
            _setterAndGetters.put( name , p );
            return p;
        }
    }

    private Object _mapGet( final String s ){
        return _mapGet( s.hashCode() , s );
    }

    private Object _mapGet( final int hash , final String s ){
        if ( _map == null )
            return null;
        final Object o = _map.get( hash , s );
        if ( o == UNDEF )
            return null;
        return o;
    }

    private void _mapSet( final String s , final Object o ){
        _checkMap();
        _map.put( s , o );
    }

    private void _dirtyMyself(){
        _version++;
        _placesToLookUpdated = false;
        _dependencies = null;
        if ( _jitCache != null )
            _jitCache.clear();
    }

    /** Returns the size of this object (approximately) in bytes.
     * @return The approximate size of this object.
     */
    public long approxSize( IdentitySet seen ){
        long size = JSObjectSize.OBJ_OVERHEAD + 128;

        if ( _name != null )
            size += JSObjectSize.OBJ_OVERHEAD + ( _name.length() * 2 );

        if ( _keys != null ){
            size += 32 + ( _keys.size() * 4 ) ; // overhead for Collection
            for ( String s : _keys )
                size += ( s.length() * 2 );
        }

        if ( _map != null )
            size += _map.approxSize( seen );

        return size;
    }

    /** Returns if this is a partial object.
     * @return if this is a partial object.
     */
    public boolean isPartialObject(){
        return _isPartialObject;
    }

    /** Sets this to be a partial object. */
    public void markAsPartialObject(){
        _isPartialObject = true;
    }

    /** Forces this to be a non-partial object.  It can still be set to be a partial object, later. */
    public void forceNonPartial(){
        _isPartialObject = false;
    }

    /** Determines whether this object has been modified since last created or cleaned.
     * @return If this object has been modified.
     */
    public boolean isDirty(){
        return _dirty || _lastHash != hashCode();
    }

    /** Indicate that an object is now "clean", that is, unmodified. */
    public void markClean(){
        _dirty = false;
        _lastHash = hashCode();
    }

    //protected Map<String,Object> _map = null;
    protected FastStringMap _map = null;
    protected Map<String,Pair<JSFunction,JSFunction>> _setterAndGetters = null;
    private Collection<String> _keys = null;
    private Set<String> _dontEnum;
    private JSFunction _constructor;
    private JSObject __proto__ = null;
    private boolean _readOnly = false;
    private String _name;

    private boolean _dirty = true;
    private long _lastHash = 0;

    private boolean _isPartialObject = false;

    private final static String _setterAndGettersSetLOCK = "_setterAndGettersSetLOCK-asdhaskfhk32qsdsfdasd";

    // jit stuff

    private int _version = 0;

    private List<JSObjectBase> _dependencies = null;
    private boolean _badDepencies = false;
    private int _dependencySum = 0;

    private boolean _placesToLookUpdated = false;
    private JSObject _placesToLook[] = new JSObject[4];

    private int _getFromParentCalls = 0;
    private Map<String,Object> _jitCache;


    /** An empty, unchangeable HashSet. */
    static final Set<String> EMPTY_SET = Collections.unmodifiableSet( new HashSet<String>() );
    /** The value "undefined", which this implementation does not currently use.  */
    static final Object UNDEF = new Object(){
            public String toString(){
                return "undefined";
            }
        };

    public static class BaseThings extends JSObjectLame {

        public BaseThings(){
            init();
        }

        public Object get( Object o ){
            String name = o.toString();
            return _things.get( name );
        }

        public Object set( Object name , Object val ){
            _things.put( name.toString() , val );
            return val;
        }

        protected void init(){

            set( "__extend" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object other , Object args[] ){

                        if ( other == null )
                            return null;

                        Object blah = s.getThis();
                        if ( ! ( blah != null && blah instanceof JSObjectBase ) )
                            throw new RuntimeException( "extendt not passed real thing" );

                        if ( ! ( other instanceof JSObject ) )
                            throw new RuntimeException( "can't extend with a non-object" );

                        ((JSObjectBase)(s.getThis())).extend( (JSObject)other );
                        return null;
                    }
                } );


            set( "merge" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object other , Object args[] ){

                        if ( other == null )
                            return null;

                        Object blah = s.getThis();
                        if ( ! ( blah != null && blah instanceof JSObject ) )
                            throw new RuntimeException( "extend not passed real thing" );

                        if ( ! ( other instanceof JSObject ) )
                            throw new RuntimeException( "can't extend with a non-object" );

                        JSObjectBase n = new JSObjectBase();
                        n.extend( (JSObject)s.getThis() );
                        n.extend( (JSObject)other );

                        return n;
                    }
                } );



            set( "__include" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object other , Object args[] ){

                        if ( other == null )
                            return null;

                        if ( ! ( other instanceof JSObject ) )
                            throw new RuntimeException( "can't include with a non-object" );

                        Object blah = s.getThis();
                        if ( ! ( blah != null && blah instanceof JSObjectBase ) )
                            throw new RuntimeException( "extend not passed real thing" );

                        ((JSObjectBase)(s.getThis())).extend( (JSObject)other );
                        return null;
                    }
                } );


            set( "__send" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object name , Object args[] ){

                        JSObject obj = ((JSObject)s.getThis());
                        if ( obj == null )
                            throw new NullPointerException( "send called on a null thing" );

                        JSFunction func = ((JSFunction)obj.get( name ) );

                        if ( func == null ){
                            // this is a dirty dirty hack for namespace collisions
                            // i hate myself for even writing it in the first place
                            func = ((JSFunction)obj.get( "__" + name ) );
                        }

                        if ( func == null )
                            func = (JSFunction)s.get( name );

                        if ( func == null )
                            throw new NullPointerException( "can't find method [" + name + "] to send" );

                        return func.call( s , args );
                    }

                } );

            set( "valueOf" , new JSFunctionCalls0(){
                    public Object call( Scope s , Object args[] ){
                        return s.getThis();
                    }
                } );

            // TODO: fix.  this is totally wrong
            set( "class" , new JSFunctionCalls0(){
                    public Object call( Scope s , Object args[] ){
                        return s.getThis();
                    }
                } );

            set( "__keySet" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object name , Object args[] ){
                        JSObjectBase obj = ((JSObjectBase)s.getThis());
                        return new JSArray( obj.keySet() );
                    }
                } );

            set( "instance_methods" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object name , Object args[] ){
                        JSObjectBase obj = ((JSObjectBase)s.getThis());
                        return new JSArray( obj.keySet() );
                    }
                } );

            set( "__debug" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object name , Object args[] ){
                        JSObjectBase obj = ((JSObjectBase)s.getThis());
                        obj.debug();
                        return null;
                    }
                } );

            set( "__hashCode" , new JSFunctionCalls0(){
                    public Object call( Scope s , Object args[] ){
                        JSObjectBase obj = ((JSObjectBase)s.getThis());
                        return obj.hashCode();
                    }
                } );

            set( "is_a_q_" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object type , Object args[] ){
                        return JSInternalFunctions.JS_instanceof( s.getThis() , type );
                    }
                } );


            set( "eql_q_" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object o , Object crap[] ){
                        return s.getThis().equals( o );
                    }
                } );

            set( "_lb__rb_" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object name , Object args[] ){
                        return ((JSObjectBase)s.getThis()).get( name );
                    }
                } );

            set( "key_q_" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object name , Object args[] ){
                        if ( name == null )
                            return null;
                        return ((JSObjectBase)s.getThis()).containsKey( name.toString() );
                    }
                } );

            set( "has_key_q_" , get( "key_q_" ) );

            set( "__delete" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object name , Object args[] ){
                        return ((JSObjectBase)s.getThis()).removeField( name );
                    }
                } );

            set( "const_defined_q_" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object type , Object args[] ){
                        return s.get( type ) != null;
                    }
                } );

            set( "__defineGetter__" , new JSFunctionCalls2(){
                    public Object call( Scope s , Object name , Object func , Object args[] ){
                        if ( ! ( s.getThis() instanceof JSObjectBase ) )
                            throw new RuntimeException( "not a JSObjectBase" );

                        JSObjectBase o = (JSObjectBase)s.getThis();
                        o.setGetter( name.toString() , (JSFunction)func );
                        return null;
                    }
                } );

            set( "__defineSetter__" , new JSFunctionCalls2(){
                    public Object call( Scope s , Object name , Object func , Object args[] ){
                        if ( ! ( s.getThis() instanceof JSObjectBase ) )
                            throw new RuntimeException( "not a JSObjectBase" );

                        JSObjectBase o = (JSObjectBase)s.getThis();
                        o.setSetter( name.toString() , (JSFunction)func );
                        return null;
                    }
                } );

            set( "__lookupGetter__" , new JSFunctionCalls2(){
                    public Object call( Scope s , Object name , Object func , Object args[] ){
                        if ( ! ( s.getThis() instanceof JSObjectBase ) )
                            throw new RuntimeException( "not a JSObjectBase" );
			
                        JSObjectBase o = (JSObjectBase)s.getThis();
			return o.getGetter( name.toString() );
                    }
                } );

            set( "__lookupSetter__" , new JSFunctionCalls2(){
                    public Object call( Scope s , Object name , Object func , Object args[] ){
                        if ( ! ( s.getThis() instanceof JSObjectBase ) )
                            throw new RuntimeException( "not a JSObjectBase" );

                        JSObjectBase o = (JSObjectBase)s.getThis();
                        return o.getSetter( name.toString() );
                    }
                } );

            set( "to_i" , new JSFunctionCalls0(){
                    public Object call( Scope s , Object args[] ){
                        return JSInternalFunctions.parseNumber( s.getThis() , null );
                    }
                } );

	    set( "dontEnum" , new JSFunctionCalls1(){
		    public Object call( Scope s , Object type , Object args[] ){
			((JSObjectBase)s.getThis()).dontEnum( type.toString() );
			return null;
		    }
		});

        }

        public Collection<String> keySet( boolean includePrototype ){
            return _things.keySet();
        }

        private Map<String,Object> _things = new HashMap<String,Object>();
    }

    /** @unexpose  */
    public static final JSObject _objectLowFunctions = new BaseThings();

    private static final ThreadLocal<Boolean> _inNotFoundHandler = new ThreadLocal<Boolean>(){
        protected Boolean initialValue(){
            return false;
        }
    };
}
