// Scope.java

package ed.js.engine;

import java.lang.reflect.*;
import java.util.*;

import ed.js.*;
import ed.js.func.*;

public class Scope {
    
    public static Scope GLOBAL = new Scope( "GLOBAL" , JSBuiltInFunctions._myScope  );
    static {
        GLOBAL._locked = true;
        GLOBAL._global = true;
    }

    static class _NULL {
        
    }
    static _NULL NULL = new _NULL();
    
    public Scope( String name , Scope parent ){
        this( name , parent , null );
    }

    public Scope( String name , Scope parent , Scope alternate ){
        _name = name;
        _parent = parent;
        
        Scope alt = null;
        if ( alternate != null ){
            Scope me = getGlobal();
            Scope them = alternate.getGlobal();
            if ( me != them ){
                if ( them.hasParent( me ) ){
                    alt = them;
                }
            }
        }
        _alternate = alt;
    }

    public Scope child(){
        return new Scope( _name + ".child" , this );
    }

    public Object put( String name , Object o , boolean local ){
        
        if ( o != null && o instanceof String ) 
            o = new JSString( o.toString() );

        if ( _locked )
            throw new RuntimeException( "locked" );
        
        if ( local
             || _parent == null
             || _parent._locked 
             || ( _objects != null && _objects.containsKey( name ) )
             || _global
             ){
            
            if ( o == null )
                o = NULL;
            if ( o instanceof String) 
                o = new JSString( (String)o );
            if ( _objects == null )
                _objects = new TreeMap<String,Object>();
            _objects.put( name , o );
            return o;
        }
        
        
        _parent.put( name , o , false );
        return o;
    }
    
    public Object get( String name ){
        return get( name , _alternate );
    }
    
    public Object get( String name , Scope alt ){
        Object foo = _objects == null ? null : _objects.get( name );
        if ( foo != null ){
            if ( foo == NULL )
                return null;
            return foo;
        }
        
        if ( alt != null && _global ){
            if ( ! alt._global )
                throw new RuntimeException( "i fucked up" );
            return alt.get( name , null );
        }

        if ( _parent == null )
            return null;
        
        return _parent.get( name , alt );
    }

    public final Scope getGlobal(){
        if ( _global )
            return this;
        if ( _parent != null )
            return _parent.getGlobal();
        return null;
    }
    
    /**
     * @return true if s is a parent of this
     */
    public final boolean hasParent( Scope s ){
        if ( this == s )
            return true;
        if ( _parent == null )
            return false;
        return _parent.hasParent( s );
    }

    public JSFunction getFunction( String name ){
        Object o = get( name );
        if ( o == null )
            return null;
        
        if ( ! ( o instanceof JSFunction ) )
            throw new RuntimeException( "not a function : " + name );
        
        return (JSFunction)o;
    }

    public Scope newThis( JSFunction f ){
        _this.push( new This( new JSObjectBase( f ) ) );
        return this;
    }

    public Scope setThis( JSObject o ){
        _this.push( new This( o ) );
        return this;
    }

    public JSFunction getFunctionAndSetThis( final Object obj , final String name ){
        boolean added = false;
        
        if ( obj instanceof JSObject ){
            JSObject jsobj = (JSObject)obj;
            _this.push( new This( jsobj ) );
            added = true;
            
            Object shouldBeFunc = jsobj.get( name );
            if ( shouldBeFunc != null && ! ( shouldBeFunc instanceof JSFunction ) )
                throw new RuntimeException( name + " is not a function.  is a:" + shouldBeFunc.getClass()  );
            
            JSFunction func = (JSFunction)shouldBeFunc;
            
            if ( func != null )
                return func;

            _this.pop();
            added = false;
            
            if ( obj instanceof JSObjectBase )
                return null;
        }
        
        if ( added ){
            _this.peek()._nThis = obj;
            _this.peek()._nThisFunc = name;
        }
        else {
            _this.push( new This( obj , name ) );
        }
        return _nativeFuncCall;
    }

    public JSObject getThis(){
        return _this.peek()._this;
    }

    public JSObject clearThisNew( Object whoCares ){
        return _this.pop()._this;
    }

    public Object clearThisNormal( Object o ){
        if ( _this.size() > 0 )
            _this.pop();
        return o;
    }

    public void lock(){
        _locked = true;
    }

    public void reset(){
        if ( _locked )
            throw new RuntimeException( "can't reset locked scope" );
        _objects.clear();
        _this.clear();
    }

    public void setGlobal( boolean g ){
        _global = g;
    }
    
    final String _name;
    final Scope _parent;
    final Scope _alternate;

    boolean _locked = false;
    boolean _global = false;

    Map<String,Object> _objects;
    
    Stack<This> _this = new Stack<This>();

    static class This {
        This( JSObject o ){
            _this = o;
        }
        
        This( Object o , String n ){
            _nThis = o;
            _nThisFunc = n;
        }
        
        // js this
        JSObject _this;
        // native this
        Object _nThis;
        String _nThisFunc;
    }

    private static final Object[] EMPTY_OBJET_ARRAY = new Object[0];
    
    private static final JSFunctionCalls0 _nativeFuncCall = new JSFunctionCalls0(){
            Map< Class , Map< String , List<Method> > > _classToMethods = new HashMap< Class , Map< String , List<Method> > >();
            
            List<Method> getMethods( Class c , String n ){
                Map<String,List<Method>> m = _classToMethods.get( c );
                if ( m == null ){
                    m = new HashMap<String,List<Method>>();
                    _classToMethods.put( c , m );
                }
                
                List<Method> l = m.get( n );
                if ( l != null )
                    return l;

                l = new ArrayList<Method>();
                for ( Method method : c.getMethods() )
                    if ( method.getName().equals( n ) )
                        l.add( method );
                m.put( n , l );
                return l;
            }

            public Object call( Scope s , Object params[] ){
                
                This temp = s._this.peek();
                final Object obj = temp._nThis;
                final String name = temp._nThisFunc;
                
                if ( obj == null )
                    throw new NullPointerException( "object was null.  name was:" + name );

                methods:
                for ( Method m : getMethods( obj.getClass() , name ) ){
                
                    Class myClasses[] = m.getParameterTypes();
                    if ( myClasses != null ){
                    
                        if ( params == null )
                            params = EMPTY_OBJET_ARRAY;
                    
                        if ( myClasses.length != params.length )
                            continue;
                        
                        System.out.println( "found match with same name and # : " + m.getName() );
                        
                        for ( int i=0; i<myClasses.length; i++ ){
                            // null is fine with me
                            if ( params[i] == null ) 
                                continue;
                            
                            Class myClass = myClasses[i];

                            if ( myClass == String.class )
                                params[i] = params[i].toString();
                            
                            if ( myClass.isPrimitive() ){
                                if ( myClass == Integer.TYPE || 
                                     myClass == Long.TYPE || 
                                     myClass == Double.TYPE ){
                                    myClass = Number.class;
                                }
                            }

                            if ( ! myClass.isAssignableFrom( params[i].getClass() ) ){
                                System.out.println( "\t faile b/c " + myClasses[i] + " " + params[i].getClass() );
                                continue methods;
                            }
                            
                        }
                    }
                
                    m.setAccessible( true );
                    try {
                        Object ret = m.invoke( obj , params );
                        if ( ret != null ){
                            if ( ret instanceof String )
                                ret = new JSString( ret.toString() );
                        }
                        return ret;
                    }
                    catch ( Exception e ){
                        throw new RuntimeException( e );
                    }
                }
                throw new RuntimeException( "can't find a valid native method for : " + name + " which  is a : " + obj.getClass()  );
            }
        };
}
