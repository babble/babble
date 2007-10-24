// TestDB.java

package ed.db;

import java.util.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;

public class TestDB extends DBBase {
    
    public DBCollection getCollection( String name ){
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
            super( name );
            _entries.put( "debug" , new JSFunctionCalls0(){
                    public Object call( Scope s , Object foo[] ){
                        return _objects.keySet().toString();
                    }

                } );
        }
        
        public JSObject save( JSObject o ){
            ObjectId id = apply( o );
            _objects.put( id , o );
            return o;
        }

        public ObjectId apply( JSObject o ){
            ObjectId id = (ObjectId)o.get( "_id" );

            if ( id == null ){
                id = ObjectId.get();
                o.set( "_id" , id );
            }
            
            return id;
        }
        
        public JSObject find( ObjectId id ){
            return _objects.get( id );
        }

        public List<JSObject> find( JSObject obj ){

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
            
            return lst;
        }
        
        Map<ObjectId,JSObject> _objects = new HashMap<ObjectId,JSObject>();
    }
    
    private Map<String,DBCollection> _collections = new TreeMap<String,DBCollection>();
}
