// DBCollection.java

package ed.db;

import java.util.*;
import java.lang.reflect.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.util.*;

/** DB Collection
 * 
 * Class for database collection objects.  When you invoke something like:
 *   var my_coll = db.students;
 * you get back a collection which can be used to perform various database operations.
 *
 * @anonymous name : {base}, desc : {The name of the database containing this collection.} type : {String}, isField : {true}
 * @anonymous name : {name}, desc : {The name of this collection}, type : {String}, isField : {true}
 * @anonymous name : {save}, desc : {Saves an object to the collection.}, return : {type : (JSObject), desc : (new object from the collection)}, param : {type : (JSObject), name : (o), desc : (object to save)}
 * @anonymous name : {update}, desc : {Updates an object in the collection.}, return : {type : (JSObject), desc : (the updated object)}, param : {type : (JSObject), name : (o), desc : (object to update)}, param : { type : (JSObject), name : (newo), desc : (object with which to update the old object)}, param : { type : (JSObject), name : (opts), desc : (boolean options to set for update)}
 * @anonymous name : {remove}, desc : {Removes an object from this collection.}, return : {type : (int), desc : (-1)}, param : {type : (JSObject), name : (q), desc : (removes object that match this query)}
 * @anonymous name : {apply}, desc : {Prepares an object for insertion into the collection.}, return : {type : (JSObject), desc : (given object with added "hidden" database fields)}, param : {type : (JSObject), name : (o), desc : (object to prepare) }
 * @anonymous name : {find}, desc : {Finds objects in this collection matching a given query.}, return : {type : (DBCursor), desc : (a cursor over any matching elements)}, param : {type : (JSObject), name : (query), desc : (query to use)}, param : { type : (JSObject), name : (f), desc : (fields to return)
 * @anonymous name : {findOne}, desc : {Returns the first object in this collection matching a given query.}, return : {type : (JSObject), desc : (the first matching element)}, param : {type : (JSObject), name : (query), desc : (query to use)}, param : { type : (JSObject), name : (f), desc : (fields to return)
 * @anonymous name : {tojson}, desc : {Returns a description of this collection as a string.}, return : {type : (String), desc : ("{DBCollection:this.collection.name}")}
 * @expose
 * @docmodule system.database.collection
 */
public abstract class DBCollection extends JSObjectLame implements Sizable {

    /** @unexpose */
    final static boolean DEBUG = Boolean.getBoolean( "DEBUG.DB" );

    /** Saves an object to the database.
     * @param o object to save
     * @return the new database object
     */
    protected abstract JSObject doSave( JSObject o );

    /** Performs an update operation.
     * @param q search query for old object to update
     * @param o object with which to update <tt>q</tt>
     * @param upsert if the database should create the element if it does not exist
     * @param apply if an _id field should be added to the new object
     * See www.10gen.com/wiki/db.update
     */
    public abstract JSObject update( JSObject q , JSObject o , boolean upsert , boolean apply );

    /** Adds any necessary fields to a given object before saving it to the collection.
     * @param o object to which to add the fields
     */
    protected abstract void doapply( JSObject o );

    /** Removes an object from the database collection.
     * @param id The _id of the object to be removed
     * @return -1
     */
    public abstract int remove( JSObject id );

    /** Finds an object by its id.
     * @param id the id of the object
     * @return the object, if found
     */
    protected abstract JSObject dofind( ObjectId id );

    /** Finds an object.
     * @param ref query used to search
     * @param fields the fields of matching objects to return
     * @param numToSkip will not return the first <tt>numToSkip</tt> matches
     * @param numToReturn limit the results to this number
     * @return the objects, if found
     */
    public abstract Iterator<JSObject> find( JSObject ref , JSObject fields , int numToSkip , int numToReturn );

    /** Ensures an index on this collection (that is, the index will be created if it does not exist).
     * ensureIndex is optimized and is inexpensive if the index already exists.
     * @param keys fields to use for index
     * @param name an identifier for the index
     */
    public abstract void ensureIndex( JSObject keys , String name );

    // ------

    /** Finds an object by its id.
     * @param id the id of the object
     * @return the object, if found
     */
    public final JSObject find( ObjectId id ){
        ensureIDIndex();

        JSObject ret = dofind( id );

        if ( ret == null )
            return null;

        apply( ret , false );

        return ret;
    }

    public final JSObject find( String id ){
        if ( ! ObjectId.isValid( id ) )
            throw new IllegalArgumentException( "invalid object id [" + id + "]" );
        return find( new ObjectId( id ) );
    }

    /** Ensures an index on the id field, if one does not already exist.
     * @param key an object with an _id field.
     */
    public void checkForIDIndex( JSObject key ){
        if ( _checkedIdIndex ) // we already created it, so who cares
            return;

        if ( key.get( "_id" ) == null )
            return;

        if ( key.keySet( false ).size() > 1 )
            return;

        ensureIDIndex();
    }

    /** Creates an index on the id field, if one does not already exist.
     * @param key an object with an _id field.
     */
    public void ensureIDIndex(){
        if ( _checkedIdIndex )
            return;

        ensureIndex( _idKey );
        _checkedIdIndex = true;
    }

    /** Creates an index on a set of fields, if one does not already exist.
     * @param keys an object with a key set of the fields desired for the index
     */
    public final void ensureIndex( final JSObject keys ){
        ensureIndex( keys , false );
    }

    /** Forces creation of an index on a set of fields, if one does not already exist.
     * @param keys an object with a key set of the fields desired for the index
     */
    public final void createIndex( final JSObject keys ){
        ensureIndex( keys , true );
    }

    /** Creates an index on a set of fields, if one does not already exist.
     * @param keys an object with a key set of the fields desired for the index
     * @param force if index creation should be forced, even if it is unnecessary
     */
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

    /** Clear all indices on this collection. */
    public void resetIndexCache(){
        _createIndexes.clear();
    }

    /** Generate an index name from the set of fields it is over.
     * @param keys the names of the fields used in this index
     * @return a string representation of this index's fields
     */
    public String genIndexName( JSObject keys ){
        String name = "";
        for ( String s : keys.keySet( false ) ){
            if ( name.length() > 0 )
                name += "_";
            name += s + "_";
            Object val = keys.get( s );
            if ( val instanceof Number )
                name += JSInternalFunctions.JS_toString( val ).replace( ' ' , '_' );
        }
        return name;
    }

    /** Queries for an object in this collection.
     * @param ref object for which to search
     * @return an iterator over the results
     */
    public final Iterator<JSObject> find( JSObject ref ){
        return find( ref == null ? new JSObjectBase() : ref , null , 0 , 0 );
    }

    public final Iterator<JSObject> find(){
        return find( new JSObjectBase() , null , 0 , 0 );
    }

    public final JSObject findOne(){
        return findOne( new JSObjectBase() );
    }

    public final JSObject findOne( JSObject o ){
        Iterator<JSObject> i = find( o , null , 0 , 1 );
        if ( i == null || ! i.hasNext() )
            return null;
        return i.next();
    }

    /**
     */
    public final ObjectId apply( JSObject o ){
        return apply( o , true );
    }
    
    /** Adds the "private" fields _save, _update, and _id to an object.
     * @param o object to which to add fields
     * @param ensureID if an _id field is needed
     * @return the _id assigned to the object
     * @throws RuntimeException if <tt>o</tt> is not a JSObject
     */
    public final ObjectId apply( JSObject jo , boolean ensureID ){

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

    /** Sets a constructor to use for objects in this collection.
     * @param cons the constructor to use
     */
    public void setConstructor( JSFunction cons ){
        _constructor = cons;
    }

    /** Returns the constructor for this collection.
     * @return the constructor function
     */
    public JSFunction getConstructor(){
	return _constructor;
    }

    /** Saves an object to this collection.
     * @param o the object to save
     * @return the new object from the collection
     */
    public final JSObject save( JSObject o ){
        if ( checkReadOnly( true ) ) return null;
        return save( null , o );
    }

    public final Map save( Map m ){
        if ( m instanceof JSObject )
            return (Map)( save( null , (JSObject)m ) );
        JSDict d = new JSDict( m );
        save( null , d );
        if ( m.get( "_id" ) == null && d.get( "_id" ) != null )
            m.put( "_id" , d.get( "_id" ) );
        return m;
    }

    /** Saves an object to this collection executing the preSave function in a given scope.
     * @param s scope to use (can be null)
     * @param o the object to save
     * @return the new object from the collection
     */
    public final JSObject save( Scope s , JSObject jo ){
        if ( checkReadOnly( true ) ) return jo;
        jo = _handleThis( s , jo );

        _checkObject( jo , false , false );

        if ( s != null ){
            Object presaveObject = jo.get( "preSave" );
	    if ( presaveObject == null )
		presaveObject = jo.get( "presave" ); // TODO: we should deprecate
	    
            if ( presaveObject != null ){
                if ( presaveObject instanceof JSFunction ){
                    s.setThis( jo );
                    ((JSFunction)presaveObject).call( s );
                    s.clearThisNormal( null );
                }
                else {
                    System.out.println( "warning, preSave is a " + presaveObject.getClass() );
                }
            }

            _findSubObject( s , jo , null );
        }

        ObjectId id = null;
	{ 
	    Object temp = jo.get( "_id" );
	    if ( temp != null && ! ( temp instanceof ObjectId ) )
		throw new RuntimeException( "_id has to be an ObjectId" );
	    id = (ObjectId)temp;
            DBRef.objectSaved( id );
	}
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
        return (JSObject)(_update.call( s , q , jo , _upsertOptions ));
    }

    // ------

    /** Initializes a new collection.
     * @param base database in which to create the collection
     * @param name the name of the collection
     */
    protected DBCollection( DBBase base , String name ){
        _base = base;
        _name = name;
        _fullName = _base.getName() + "." + name;

        _entries.put( "base" , _base.getName() );
        _entries.put( "name" , _name );
        
        _save = new JSFunctionCalls1() {
                public Object call( Scope s , Object o , Object fooasd[] ){
                    _anyUpdateSave = true;
                    if ( o == null )
                        o = _handleThis( s , null );
                    return save( s , _checkObject( o , false , false ) );
                }
            };
        _entries.put( "save" , _save );


        _update = new JSFunctionCalls4(){
                public Object call( Scope s , Object q , Object o , Object options , Object seen , Object foo[] ){
                    if ( checkReadOnly( true ) ) return o;

                    _anyUpdateSave = true;

                    _checkObject( q , false , true );
                    _checkObject( o , false , true ); // TODO: updates require special semantics

                    if ( s != null )
                        _findSubObject( s , (JSObject)o , (IdentitySet)seen );

                    boolean upsert = false;
                    boolean apply = true;

		    /* this is for $inc and $set: we don't add an object id then. see struct Mod in p/db/query.cpp */
                    if ( o instanceof JSObject ) { 
			apply = false;
			for( String key : ((JSObject)o).keySet() ){
			    if ( ! key.startsWith( "$" ) ){
				apply = true;
				break;
			    }
			}
		    }
		    //                    if ( o instanceof JSObject && ((JSObject)o).containsKey( "$inc" ) )
		    //                        apply = false;

                    if ( options instanceof JSObject ){
                        JSObject params = (JSObject)options;

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
                              
			      o = _massageObjectToFilter( o , false , true );

                              _checkObject( o , true , true );

                              JSObject jo = _handleThis( s , (JSObject)o );

                              if ( o == null )
                                  throw new NullPointerException( "can't pass null to collection.remove. if you mean to remove everything, do remove( {} ) " );

                              return remove( jo );

                          }
                      } );




        _apply = new JSFunctionCalls1() {
                public Object call( Scope s , Object o , Object foo[] ){
                    return apply( _checkObject( o , false , false ) );
                }
            };
        _entries.put( "apply" , _apply );

        _find = new JSFunctionCalls2() {
                public Object call( Scope s , Object o , Object fieldsWantedO , Object foo[] ){
                    
		    o = _massageObjectToFilter( o , true , true );
                    
                    if ( o instanceof JSObject ){
                        JSObject key = (JSObject)o;
                        checkForIDIndex( key );
                        return new DBCursor( DBCollection.this , key , (JSObject)fieldsWantedO , _constructor );
                    }

                    throw new IllegalArgumentException( "invalid type for db find [" + o.getClass().getName() + "]" );
                }
            };
        _entries.put( "find" , _find );

        _entries.put( "findOne" ,
                      new JSFunctionCalls1() {
                          public Object call( Scope s , Object o , Object foo[] ){
                              
                              o = _massageObjectToFilter( o , true , false );
                              if ( o instanceof ObjectId && ( foo == null || foo.length == 0 || foo[0] == null ) ){
                                  ensureIDIndex();
                                  return find( (ObjectId)o );
                              }

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
                              return DBCollection.this._fullName;
                          }
                      } );
    }

    Object _massageObjectToFilter( Object o , boolean maskNull , boolean convertIdToObject ){
	if ( o == null ){
	    if ( maskNull )
		return new JSObjectBase();
	    return null;
	}
	
	if ( o instanceof JSFunction && ((JSFunction)o).isCallable() && ((JSFunction)o).getSourceCode() != null ){
	    JSObjectBase obj = new JSObjectBase();
	    obj.set( "$where" , o );
	    return obj;
	}

	if ( o instanceof DBRef )
	    o = ((DBRef)o)._id;

	if ( o instanceof String || o instanceof JSString ){
	    String str = o.toString();
	    if ( ObjectId.isValid( str ) )
		o = new ObjectId( str );
	    else 
		throw new IllegalArgumentException( "can't call find() with a string that is not a valid ObjectId" );
	}

	if ( convertIdToObject && o instanceof ObjectId ){
	    JSObjectBase obj = new JSObjectBase();
	    obj.set( "_id" , o );
	    return obj;
	}

	return o;
	
    }

    protected void _finishInit(){
        if ( _name.equals( "_file" ) )
            JSFile.setup( this );
        else if ( _name.equals( "_chunks" ) )
            JSFileChunk.setup( this );

    }

    private final JSObject _handleThis( Scope s , JSObject o ){
        if ( o != null )
            return o;

        Object t = s.getThis();
        if ( t == null )
            return null;

        if ( ! ( t instanceof JSObject ) )
            return null;

        if ( t.getClass() != JSObjectBase.class )
            return null;

        return (JSObject)t;
    }

    private final JSObject _checkObject( Object o , boolean canBeNull , boolean query ){
        if ( o == null ){
            if ( canBeNull )
                return null;
            throw new NullPointerException( "can't be null" );
        }

        if ( ! ( o instanceof JSObject ) )
            throw new IllegalArgumentException( " has to be a JSObject not : " + o.getClass() );
        
        if ( o instanceof JSObjectBase &&
             ((JSObjectBase)o).isPartialObject() )
            throw new IllegalArgumentException( "can't save partial objects" );

        JSObject jo = (JSObject)o;
        if ( ! query ){
            for ( String s : jo.keySet() ){
                if ( s.contains( "." ) )
                    throw new IllegalArgumentException( "fields stored in the db can't have . in them" );
                if ( s.contains( "$" ) )
                    throw new IllegalArgumentException( "fields stored in the db can't have $ in them" );
            }
        }
        return jo;
    }

    private void _findSubObject( Scope s , JSObject jo , IdentitySet seenSubs ){
        if ( seenSubs == null )
            seenSubs = new IdentitySet();

        if ( seenSubs.contains( jo ) )
            return;
        seenSubs.add( jo );
        

        if ( DEBUG ) System.out.println( "_findSubObject on : " + jo.get( "_id" ) );

        LinkedList<JSObject> toSearch = new LinkedList();
        Map<JSObject,String> seen = new IdentityHashMap<JSObject,String>();
        toSearch.add( jo );

        while ( toSearch.size() > 0 ){
            
            Map<JSObject,String> seenNow = new IdentityHashMap<JSObject,String>( seen );
            
            JSObject n = toSearch.remove(0);
            for ( String name : n.keySet( false ) ){
                Object foo = n.get( name );
                if ( foo == null )
                    continue;

                if ( ! ( foo instanceof JSObject ) )
                    continue;

                if ( foo instanceof JSFunction )
                    continue;

		if ( foo instanceof JSString 
                     || foo instanceof JSRegex
                     || foo instanceof JSDate )
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
                        throw new RuntimeException( "you have a loop. key : " + name + " from a " + n.getClass()  + " which is a : " + e.getClass() );
                    seenNow.put( e , "a" );
                    toSearch.add( e );
                    continue;
                }


                // ok - now we knows its a reference

                if ( e.get( "_id" ) == null ){ // new object, lets save it
                    JSFunction otherSave = e.getFunction( "_save" );
                    if ( otherSave == null )
                        throw new RuntimeException( "no save :(" );
                    otherSave.call( s , e , null );
                    continue;
                }

                // old object, lets update TODO: dirty tracking
                JSObject lookup = new JSObjectBase();
                lookup.set( "_id" , e.get( "_id" ) );

                JSFunction otherUpdate = e.getFunction( "_update" );
                if ( otherUpdate == null ){

                    // already taken care of
                    if ( e instanceof DBRef )
                        continue;

                    throw new RuntimeException( "_update is null.  keyset : " + e.keySet( false ) + " ns:" + e.get( "_ns" ) );
                }

                if ( e instanceof JSObjectBase && ! ((JSObjectBase)e).isDirty() )
                    continue;

                otherUpdate.call( s , lookup , e , _upsertOptions , seenSubs );

            }
            
            seen.putAll( seenNow );
        }
    }

    /** Gets any type of object matching the given object from this collection's database.
     * @param n object to find
     * @return the object, if found
     */
    public Object get( Object n ){
        if ( n == null )
            return null;
        Object foo = _entries.get( n.toString() );
        if ( foo != null )
            return foo;

        foo = _base._collectionPrototype.get( n );
        if ( foo != null )
            return foo;

        String s = n.toString();
        
        if ( _getJavaMethods().contains( s ) )
            return null;

        return getCollection( s );
    }

    public Object set( Object n , Object v ){
        return _entries.put( n.toString() , v );
    }

    public Set<String> keySet( boolean includePrototype ){
        Set<String> set = new HashSet<String>();
        
        set.addAll( _entries.keySet() );
        set.addAll( _base._collectionPrototype.keySet() );
        set.addAll( _getJavaMethods() );

        // TODO: ? add sub collection names
        return set;
    }

    /** Find a collection that is prefixed with this collection's name.
     * @param n the name of the collection to find
     * @return the matching collection
     */
    public DBCollection getCollection( String n ){
        return _base.getCollection( _name + "." + n );
    }

    /** Returns the name of this collection.
     * @return  the name of this collection
     */
    public String getName(){
        return _name;
    }

    /** Returns the full name of this collection, with the database name as a prefix.
     * @return  the name of this collection
     */
    public String getFullName(){
        return _fullName;
    }

    /** Returns the database this collection is a member of.
     * @return this collection's database
     */
    public DBBase getDB(){
        return _base;
    }

    /** Returns the database this collection is a member of.
     * @return this collection's database
     */
    public DBBase getBase(){
        return _base;
    }

    /** Returns if this collection can be modified.
     * @return if this collection can be modified
     */
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

    /** Calculates the hash code for this collection.
     * @return the hash code
     */
    public int hashCode(){
        return _fullName.hashCode();
    }

    /** Checks if this collection is equal to another object.
     * @param o object with which to compare this collection
     * @return if the two collections are equal
     */
    public boolean equals( Object o ){
        return o == this;
    }

    /** Returns name of the collection.
     * @return name of the collection.
     */
    public String toString(){
        return _name;
    }

    public long approxSize( IdentitySet seen ){
	long size = 256;
	if ( _constructor != null ){
	    if ( ! seen.contains( _constructor ) ){
		seen.add( _constructor );
		size += _constructor.approxSize( seen );
	    }
	}
	return size;
    }

    private Set<String> _getJavaMethods(){
        if ( _javaMethods == null ){
            Set<String> temp = new HashSet<String>();
            for ( Method m : this.getClass().getMethods() )
                temp.add( m.getName() );
            _javaMethods = temp;
        }
        return _javaMethods;
    }
    
    final DBBase _base;

    final JSFunction _save;
    final JSFunction _update;
    final JSFunction _apply;
    final JSFunction _find;

    static Set<String> _javaMethods;

    protected Map _entries = new TreeMap();
    final protected String _name;
    final protected String _fullName;

    protected JSFunction _constructor;

    private boolean _anyUpdateSave = false;

    private boolean _checkedIdIndex = false;
    final private Set<String> _createIndexes = new HashSet<String>();
    final private Set<String> _createIndexesAfterSave = new HashSet<String>();

    private final static JSObjectBase _upsertOptions = new JSObjectBase();
    static {
        _upsertOptions.set( "upsert" , true );
        _upsertOptions.setReadOnly( true );
    }

    private final static JSObjectBase _idKey = new JSObjectBase();
    static {
        _idKey.set( "_id" , ObjectId.get() );
        _idKey.setReadOnly( true );
    }
}
