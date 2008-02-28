// DBCollection.java

package ed.db;

import java.util.*;
import java.lang.reflect.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;

public abstract class DBCollection extends JSObjectLame {

    final static boolean DEBUG = Boolean.getBoolean( "DEBUG.DB" );
    
    protected abstract JSObject doSave( JSObject o );
    public abstract JSObject update( JSObject q , JSObject o , boolean upsert , boolean apply );

    protected abstract ObjectId doapply( JSObject o );
    public abstract int remove( JSObject id );
    
    public abstract JSObject find( ObjectId id );    
    public abstract Iterator<JSObject> find( JSObject ref , JSObject fields , int numToSkip , int numToReturn );

    public abstract void ensureIndex( JSObject keys , String name );

    // ------

    public void ensureIndex( final JSObject keys ){
        if ( checkReadOnly() ) return;

        final String name = genIndexName( keys );

        boolean doEnsureIndex = false;
        if ( Math.random() > 0.999 )
            doEnsureIndex = true;
        else if ( ! _createIndexes.contains( name ) )
            doEnsureIndex = true;
        else if ( _anyUpdateSave && ! _createIndexesAfterSave.contains( name ) )
            doEnsureIndex = true;

        if ( ! doEnsureIndex )
            return;
        
        ensureIndex( keys , name );
        
        _createIndexes.add( name );
        if ( _anyUpdateSave )
            _createIndexesAfterSave.add( name );
    }

    public String genIndexName( JSObject keys ){
        String name = "";
        for ( String s : keys.keySet() ){
            if ( name.length() > 0 )
                name += "_";
            name += s + "_" + keys.get( s ).toString().replace( ' ' , '_' );
        }
        return name;
    }

    public Iterator<JSObject> find( JSObject ref ){
        return find( ref , null , 0 , 0 );
    }

    public ObjectId apply( Object o ){

        if ( ! ( o instanceof JSObject ) )
            throw new RuntimeException( "can only apply JSObject" );
        
        JSObject jo = (JSObject)o;
        jo.set( "_save" , _save );
        jo.set( "_update" , _update );
        
        return doapply( jo );
    }

    public void setConstructor( JSFunction cons ){
        _constructor = cons;
    }

    
    public final Object save( Object o ){
        if ( checkReadOnly() ) return null;
        return save( null , o );
    }
        
    public final Object save( Scope s , Object o ){
        if ( checkReadOnly() ) return o;
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
                    if ( checkReadOnly() ) return o;

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
                              if ( checkReadOnly() ) return o;
                              
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
                              return "{DBCollection:" + _name + "}";
                          }
                      } );

        _entries.put( "getIndexes" , Convert.makeAnon( "return this.getBase().system.indexes.find( { ns : /^\\w+\\." + getName() + "$/ } );" ) );
        
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

        if ( o instanceof JSObject )
            return;
        
        throw new IllegalArgumentException( " has to be a JSObject not : " + o.getClass() );
    }

    private void _findSubObject( Scope s , JSObject jo ){

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
                if ( otherUpdate == null )
                    throw new RuntimeException( "_update is null :(" );
                
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
        
        if ( _methods.size() == 0 )
            for ( Method m : this.getClass().getMethods() )
                _methods.add( m.getName() );

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

    public DBBase getBase(){
        return _base;
    }

    protected boolean checkReadOnly(){
        if ( ! _base._readOnly )
            return false;
        
        Scope scope = Scope.getThredLocal();
        if ( scope != null ){
            Object foo = scope.get( "dbStrict" );
            if ( foo != null && JSInternalFunctions.JS_evalToBool( foo ) )
                throw new JSException( "db is read only" );
        }

        return true;
    }
    
    final DBBase _base;
    
    final JSFunction _save;
    final JSFunction _update;
    final JSFunction _apply;
    final JSFunction _find;
    
    final Set<String> _methods = new HashSet<String>();

    protected Map _entries = new TreeMap();
    final protected String _name;
    
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
