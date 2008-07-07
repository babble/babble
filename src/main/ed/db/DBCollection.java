// DBCollection.java

package ed.db;

import java.util.*;
import java.lang.reflect.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;

/** DB Collection
 * @expose
 */
public abstract class DBCollection extends JSObjectLame {

    final static boolean DEBUG = Boolean.getBoolean( "DEBUG.DB" );

    protected abstract JSObject doSave( JSObject o );
    public abstract JSObject update( JSObject q , JSObject o , boolean upsert , boolean apply );

    protected abstract void doapply( JSObject o );
    public abstract int remove( JSObject id );

    protected abstract JSObject dofind( ObjectId id );
    public abstract Iterator<JSObject> find( JSObject ref , JSObject fields , int numToSkip , int numToReturn );

    public abstract void ensureIndex( JSObject keys , String name );

    // ------

    public final JSObject find( ObjectId id ){

        JSObject ret = dofind( id );

        if ( ret == null )
            return null;

        apply( ret , false );

        return ret;
    }

    public final void ensureIndex( final JSObject keys ){
        ensureIndex( keys , false );
    }

    public final void createIndex( final JSObject keys ){
        ensureIndex( keys , true );
    }

    public final void ensureIndex( final JSObject keys , final boolean force ){
        if ( checkReadOnly( false ) ) return;

        final String name = genIndexName( keys );

        boolean doEnsureIndex = false;
        if ( Math.random() > 0.999 )
            doEnsureIndex = true;
        else if ( ! _createIndexes.contains( name ) )
            doEnsureIndex = true;
        else if ( _anyUpdateSave && ! _createIndexesAfterSave.contains( name ) )
            doEnsureIndex = true;

        if ( ! ( force || doEnsureIndex ) )
            return;

        ensureIndex( keys , name );

        _createIndexes.add( name );
        if ( _anyUpdateSave )
            _createIndexesAfterSave.add( name );
    }

    public void resetIndexCache(){
        _createIndexes.clear();
    }

    public String genIndexName( JSObject keys ){
        String name = "";
        for ( String s : keys.keySet() ){
            if ( name.length() > 0 )
                name += "_";
            name += s + "_";
            Object val = keys.get( s );
            if ( ! ( val instanceof ObjectId ) )
                name += JSInternalFunctions.JS_toString( val ).replace( ' ' , '_' );
        }
        return name;
    }

    public final Iterator<JSObject> find( JSObject ref ){
        return find( ref , null , 0 , 0 );
    }

    public final ObjectId apply( Object o ){
        return apply( o , true );
    }

    public final ObjectId apply( Object o , boolean ensureID ){

        if ( ! ( o instanceof JSObject ) )
            throw new RuntimeException( "can only apply JSObject" );

        JSObject jo = (JSObject)o;
        jo.set( "_save" , _save );
        jo.set( "_update" , _update );

        ObjectId id = (ObjectId)jo.get( "_id" );
        if ( ensureID && id == null ){
            id = ObjectId.get();
            jo.set( "_id" , id );
        }

        doapply( jo );

        return id;
    }

    public void setConstructor( JSFunction cons ){
        _constructor = cons;
    }

    public JSFunction getConstructor(){
	return _constructor;
    }

    public final Object save( Object o ){
        if ( checkReadOnly( true ) ) return null;
        return save( null , o );
    }

    public final Object save( Scope s , Object o ){
        if ( checkReadOnly( true ) ) return o;
        o = _handleThis( s , o );

        _checkObject( o , false );

        JSObject jo = (JSObject)o;

        if ( s != null ){
            Object presaveObject = jo.get( "presave" );
            if ( presaveObject != null ){
                if ( presaveObject instanceof JSFunction ){
                    s.setThis( jo );
                    ((JSFunction)presaveObject).call( s );
                    s.clearThisNormal( null );
                }
                else {
                    System.out.println( "warning, presave is a " + presaveObject.getClass() );
                }
            }

            _findSubObject( s , jo );
        }

        ObjectId id = (ObjectId)jo.get( "_id" );
        if ( DEBUG ) System.out.println( "id : " + id );

        if ( id == null || id._new ){
            if ( DEBUG ) System.out.println( "saving new object" );
            if ( id != null )
                id._new = false;
            doSave( jo );
            return jo;
        }

        if ( DEBUG ) System.out.println( "doing implicit upsert : " + jo.get( "_id" ) );
        JSObject q = new JSObjectBase();
        q.set( "_id" , id );
        return _update.call( s , q , jo , _upsertOptions );
    }

    // ------

    protected DBCollection( DBBase base , String name ){
        _base = base;
        _name = name;
        _fullName = _base.getName() + "." + name;

        _entries.put( "base" , _base.getName() );
        _entries.put( "name" , _name );

        _save = new JSFunctionCalls1() {
                public Object call( Scope s , Object o , Object fooasd[] ){
                    _anyUpdateSave = true;
                    return save( s , o );
                }
            };
        _entries.put( "save" , _save );


        _update = new JSFunctionCalls2() {
                public Object call( Scope s , Object q , Object o , Object foo[] ){
                    if ( checkReadOnly( true ) ) return o;

                    _anyUpdateSave = true;

                    _checkObject( q , false );
                    _checkObject( o , false );

                    if ( s != null )
                        _findSubObject( s , (JSObject)o );

                    boolean upsert = false;
                    boolean apply = true;

                    if ( o instanceof JSObject && ((JSObject)o).containsKey( "$inc" ) )
                        apply = false;

                    if ( foo != null && foo.length > 0 && foo[0] instanceof JSObject ){
                        JSObject params = (JSObject)foo[0];

                        upsert = JSInternalFunctions.JS_evalToBool( params.get( "upsert" ) );
                        if ( params.get( "ids" ) != null )
                            apply = JSInternalFunctions.JS_evalToBool( params.get( "ids" ) );
                    }

                    return update( (JSObject)q , (JSObject)o , upsert , apply );
                }
            };
        _entries.put( "update" , _update );

        _entries.put( "remove" ,
                      new JSFunctionCalls1(){
                          public Object call( Scope s , Object o , Object foo[] ){
                              if ( checkReadOnly( true ) ) return o;

                              o = _handleThis( s , o );

                              if ( ! ( o instanceof JSObject ) )
                                  throw new RuntimeException( "can't only save JSObject" );

                              return remove( (JSObject)o );

                          }
                      } );




        _apply = new JSFunctionCalls1() {
                public Object call( Scope s , Object o , Object foo[] ){
                    return apply( o );
                }
            };
        _entries.put( "apply" , _apply );

        _find = new JSFunctionCalls2() {
                public Object call( Scope s , Object o , Object fieldsWantedO , Object foo[] ){

                    if ( o == null )
                        o = new JSObjectBase();

                    if ( o instanceof ObjectId )
                        return find( (ObjectId)o );

                    if ( o instanceof JSObject ){
                        return new DBCursor( DBCollection.this , (JSObject)o , (JSObject)fieldsWantedO , _constructor );
                    }

                    throw new RuntimeException( "wtf : " + o.getClass() );
                }
            };
        _entries.put( "find" , _find );

        _entries.put( "findOne" ,
                      new JSFunctionCalls1() {
                          public Object call( Scope s , Object o , Object foo[] ){
                              Object res = _find.call( s , o , foo );
                              if ( res == null )
                                  return null;

			      if ( res instanceof DBCursor )
				  ((DBCursor)res).limit( 1 );

                              if ( res instanceof JSArray ){
                                  JSArray a = (JSArray)res;
                                  if ( a.size() == 0 )
                                      return null;
                                  return a.getInt( 0 );
                              }

                              if ( res instanceof Iterator ){
                                  Iterator<JSObject> it = (Iterator<JSObject>)res;
                                  if ( ! it.hasNext() )
                                      return null;
                                  return it.next();
                              }

                              if ( res instanceof JSObject )
                                  return res;

                              throw new RuntimeException( "wtf : " + res.getClass() );
                          }
                      } );

        _entries.put( "tojson" ,
                      new JSFunctionCalls0() {
                          public Object call( Scope s , Object foo[] ){
                              return "{DBCollection:" + DBCollection.this._fullName + "}";
                          }
                      } );

    }

    private final Object _handleThis( Scope s , Object o ){
        if ( o != null )
            return o;

        Object t = s.getThis();
        if ( t == null )
            return null;

        if ( t.getClass() != JSObjectBase.class )
            return null;

        return o;
    }

    private final void _checkObject( Object o , boolean canBeNull ){
        if ( o == null ){
            if ( canBeNull )
                return;
            throw new NullPointerException( "can't be null" );
        }
        
        if ( o instanceof JSObjectBase && 
             ((JSObjectBase)o).isPartialObject() )
            throw new IllegalArgumentException( "can't save partial objects" );

        if ( o instanceof JSObject )
            return;
        
        throw new IllegalArgumentException( " has to be a JSObject not : " + o.getClass() );
    }

    private void _findSubObject( Scope s , JSObject jo ){

        if ( DEBUG ) System.out.println( "_findSubObject on : " + jo.get( "_id" ) );

        LinkedList<JSObject> toSearch = new LinkedList();
        Map<JSObject,String> seen = new IdentityHashMap<JSObject,String>();
        toSearch.add( jo );

        while ( toSearch.size() > 0 ){
            JSObject n = toSearch.remove(0);
            for ( String name : n.keySet() ){
                Object foo = n.get( name );
                if ( foo == null )
                    continue;

                if ( ! ( foo instanceof JSObject ) )
                    continue;

                if ( foo instanceof JSFunction )
                    continue;

		if ( foo instanceof JSString )
		    continue;

                JSObject e = (JSObject)foo;
                if ( e instanceof JSObjectBase )
                    ((JSObjectBase)e).prefunc();
                if ( n.get( name ) == null )
                    continue;

                if ( e instanceof JSFileChunk ){
                    _base.getCollection( "_chunks" ).apply( e );
                }

                if ( e.get( "_ns" ) == null ){
                    if ( seen.containsKey( e ) )
                        throw new RuntimeException( "you have a loop. key : " + name + " from a " + n.getClass()  + " whis is a : " + e.getClass() );
                    seen.put( e , "a" );
                    toSearch.add( e );
                    continue;
                }

                // ok - now we knows its a reference

                if ( e.get( "_id" ) == null ){ // new object, lets save it
                    JSFunction otherSave = (JSFunction)e.get( "_save" );
                    if ( otherSave == null )
                        throw new RuntimeException( "no save :(" );
                    otherSave.call( s , e , null );
                    continue;
                }

                // old object, lets update TODO: dirty tracking
                JSObject lookup = new JSObjectBase();
                lookup.set( "_id" , e.get( "_id" ) );

                JSFunction otherUpdate = (JSFunction)e.get( "_update" );
                if ( otherUpdate == null ){

                    // already taken care of
                    if ( e instanceof DBRef )
                        continue;

                    throw new RuntimeException( "_update is null.  keyset : " + e.keySet() + " ns:" + e.get( "_ns" ) );
                }

                if ( e instanceof DBRef && ! ((DBRef)e).isDirty() )
                    continue;

                otherUpdate.call( s , lookup , e , _upsertOptions );

            }
        }
    }

    public Object get( Object n ){
        if ( n == null )
            return null;
        Object foo = _entries.get( n.toString() );
        if ( foo != null )
            return foo;

        foo = _base._collectionPrototype.get( n );
        if ( foo != null )
            return foo;

        if ( _methods == null ){
            Set<String> temp = new HashSet<String>();
            for ( Method m : this.getClass().getMethods() )
                temp.add( m.getName() );
            _methods = temp;
        }

        String s = n.toString();

        if ( _methods.contains( s ) )
            return null;

        return getCollection( s );
    }

    public DBCollection getCollection( String n ){
        return _base.getCollection( _name + "." + n );
    }

    public String getName(){
        return _name;
    }

    public String getFullName(){
        return _fullName;
    }

    public DBBase getDB(){
        return _base;
    }

    public DBBase getBase(){
        return _base;
    }

    protected boolean checkReadOnly( boolean strict ){
        if ( ! _base._readOnly )
            return false;

        if ( ! strict )
            return true;

        Scope scope = Scope.getThreadLocal();
        if ( scope == null )
            throw new JSException( "db is read only" );

        Object foo = scope.get( "dbStrict" );
        if ( foo == null || JSInternalFunctions.JS_evalToBool( foo ) )
            throw new JSException( "db is read only" );

        return true;
    }

    public String toString(){
        return "{DBCollection:" + _name + "}";
    }

    final DBBase _base;

    final JSFunction _save;
    final JSFunction _update;
    final JSFunction _apply;
    final JSFunction _find;

    static Set<String> _methods;

    protected Map _entries = new TreeMap();
    final protected String _name;
    final protected String _fullName;

    protected JSFunction _constructor;

    private boolean _anyUpdateSave = false;

    final private Set<String> _createIndexes = new HashSet<String>();
    final private Set<String> _createIndexesAfterSave = new HashSet<String>();

    private final static JSObjectBase _upsertOptions = new JSObjectBase();
    static {
        _upsertOptions.set( "upsert" , true );
        _upsertOptions.setReadOnly( true );
    }
}
