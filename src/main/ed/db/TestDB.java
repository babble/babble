// TestDB.java

package ed.db;

import java.util.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;

public class TestDB extends DBBase {

    public TestDB(){
	super( "stupid" );
    }

    public String getConnectPoint(){
	return null;
    }


    public DBCollection getCollectionFromFull( String fullNameSpace ){
        throw new RuntimeException( "not implemented" );
    }
    
    protected DBCollection doGetCollection( String name ){
        DBCollection c = _collections.get( name );
        if ( c == null ){
            c = new MyCollection( name );
            _collections.put( name , c );
        }
        return c;
    }
    
    public Collection<String> getCollectionNames(){
        return Collections.unmodifiableSet( _collections.keySet() );
    }
    
    class MyCollection extends DBCollection {

        MyCollection( String name ){
            super( TestDB.this , name );
            _entries.put( "debug" , new JSFunctionCalls0(){
                    public Object call( Scope s , Object foo[] ){
                        return _objects.keySet().toString();
                    }

                } );
        }

        public void ensureIndex( JSObject keys , String name ){
        }

        public int remove( JSObject o ){
            throw new RuntimeException( "not implemented" );
        }

        public JSObject update( JSObject q , JSObject o , boolean upsert , boolean apply ){
            throw new RuntimeException( "not implemented" );
        }
        
        public JSObject doSave( JSObject o ){
            ObjectId id = apply( o );
            _objects.put( id , o );
            return o;
        }

        public void doapply( JSObject o ){
        }
        
        public JSObject dofind( ObjectId id ){
            return _objects.get( id );
        }

        public Iterator<JSObject> find( JSObject obj , JSObject fields , int numToSkip , int numToReturn ){

            List<JSObject> lst = null;
            
            all:
            for ( JSObject foo : _objects.values() ){
                for ( String p : obj.keySet() ){
                    if ( ! JSInternalFunctions.JS_eq( obj.get( p ) , foo.get( p ) ) )
                        continue all;
                }

                if ( lst == null )
                    lst = new ArrayList<JSObject>();
                lst.add( foo );
            }
            
            return lst.iterator();
        }
        
        Map<ObjectId,JSObject> _objects = new HashMap<ObjectId,JSObject>();
    }
    
    private Map<String,DBCollection> _collections = new TreeMap<String,DBCollection>();
}
