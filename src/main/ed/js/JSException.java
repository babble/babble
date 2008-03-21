// JSException.java

package ed.js;

import ed.js.func.*;
import ed.js.engine.*;

public class JSException extends RuntimeException {

    public final static JSFunction _quiteCons = new JSFunctionCalls1(){
            
            public JSObjectBase newOne(){
                throw new JSException( "don't use new" );
            }

            public Object call( Scope scope , Object msg , Object[] extra ){
                return new Quiet( msg );
            }
            
            protected void init(){

            }
        };
    

    public final static JSFunction _cons = new JSFunctionCalls1(){

            public JSObjectBase newOne(){
                throw new JSException( "don't use new" );
            }

            public Object call( Scope scope , Object msg , Object[] extra ){
                return new JSException( msg , null , true );
            }
            
            protected void init(){
                set( "Quiet" , _quiteCons );
            }
        };

    public JSException( Object o ){
        this( o , null );
    }
    
    public JSException( Object o , Throwable t ){
        this( o , t , false );
    }
    
    public JSException( Object o , Throwable t , boolean wantedJSException ){
        super( ( o == null ? "" : o.toString() ).toString() , _fix( t ) );
        _object = o;
        _wantedJSException = wantedJSException;
    }

    public Object getObject(){
        if ( _wantedJSException )
            return this;
        return _object;
    }
    
    final Object _object;
    final boolean _wantedJSException;

    static Throwable _fix( Throwable t ){
        if ( t instanceof java.lang.reflect.InvocationTargetException )
            return ((java.lang.reflect.InvocationTargetException)t).getTargetException();
        return t;
    }

    public static class Quiet extends JSException {
        public Quiet( Object msg ){
            super( msg , null , true );
        }
    }
}
