// DBCollection.java

package ed.db;

import java.util.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;

public abstract class DBCollection extends JSObjectLame {
    
    public abstract JSObject save( JSObject o );
    public abstract ObjectId apply( JSObject o );
    public abstract JSObject find( ObjectId id );
    
    /**
     * this should either be a hard list or some sort of cursor
     */
    public abstract List<JSObject> find( JSObject ref );

    protected DBCollection( String name ){
        _name = name;

        _entries.put( "name" , _name );

        _save = new JSFunctionCalls1() {
                public Object call( Scope s , Object o , Object foo[] ){
                    
                    if ( o == null && s.getThis() != null )
                        o = s.getThis();
                    
                    if ( ! ( o instanceof JSObject ) )
                        throw new RuntimeException( "can't only save JSObject" );
                    
                    return save( (JSObject)o );
                }
            };
        _entries.put( "save" , _save );
        
        _apply = new JSFunctionCalls1() {
                public Object call( Scope s , Object o , Object foo[] ){
                    if ( ! ( o instanceof JSObject ) )
                        throw new RuntimeException( "can't only apply JSObject" );
                    JSObject jo = (JSObject)o;
                    jo.set( "save" , _save );
                    return apply( jo );
                }
            };
        _entries.put( "apply" , _apply );

        _find = new JSFunctionCalls1() {
                public Object call( Scope s , Object o , Object foo[] ){
                    
                    if ( o == null )
                        o = new JSObjectBase();
                    
                    if ( o instanceof JSObject ){
                        List<JSObject> l = find( (JSObject)o );
                        if ( l == null )
                            return new JSArray();
                        return new JSArray( l );
                    }

                    if ( o instanceof ObjectId )
                        return find( (ObjectId)o );
                                        
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
                              
                              if ( res instanceof JSArray ){
                                  JSArray a = (JSArray)res;
                                  if ( a.size() == 0 )
                                      return null;
                                  return a.getInt( 0 );
                              }
                              
                              if ( res instanceof JSObject )
                                  return res;
                              
                              throw new RuntimeException( "wtf : " + res.getClass() );
                          }
                      } );
    }

    public Object get( Object n ){
        return _entries.get( n );
    }

    final JSFunction _save;
    final JSFunction _apply;
    final JSFunction _find;

    protected Map _entries = new TreeMap();
    final protected String _name;
}
