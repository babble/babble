// JSException.java

package ed.js;

import ed.js.func.*;
import ed.js.engine.*;

public class JSException extends RuntimeException implements JSObject {

    public final static JSFunction _quietCons = new JSFunctionCalls1(){
            
            public JSObjectBase newOne(){
                throw new JSException( "don't use new" );
            }

            public Object call( Scope scope , Object msg , Object[] extra ){
                return new Quiet( msg );
            }
            
            protected void init(){

            }
        };
    

    public final static JSFunction _redirectCons = new JSFunctionCalls1(){
            public JSObjectBase newOne(){
                throw new JSException( "don't use new" );
            }
            public Object call( Scope scope , Object to , Object[] extra){
                return new Redirect( to );
            }

            protected void init(){

            }
        };

    public static class cons extends JSFunctionCalls1{
        
        public JSObjectBase newOne(){
            // TODO: need a work-around for this
            return new cons();
        }
        
        public Object call( Scope scope , Object msg , Object[] extra ){
            Object foo = scope.getThis();
            if ( foo == null || ! ( foo instanceof JSException ) )
                return new JSException( msg , null , true );
            JSException e = (JSException)foo;
            e._msg = msg;
            return e;
        }
        
        protected void init(){
            set( "Quiet" , _quietCons );
            set( "Redirect" , _redirectCons );
        }
    }
    
    public final static JSFunction _cons = new cons();

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
    
    static Throwable _fix( Throwable t ){
        if ( t instanceof java.lang.reflect.InvocationTargetException )
            return ((java.lang.reflect.InvocationTargetException)t).getTargetException();
        return t;
    }

    public static class Quiet extends JSException {
        public Quiet( Object msg ){
            super( msg , null , true );
            _mycons = _quietCons;
        }
    }

    public static class Redirect extends JSException {
        public Redirect( Object location ){
            super( "redirecting" , null , true );
            _mycons = _redirectCons;

            if(location instanceof String){
                _location = (String)location;
            }
            else if(location instanceof JSString){
                _location = ((JSString)location).toString();
            }
            else {
                throw new RuntimeException("Exception.Redirect takes a string");
            }
        }
        
        public String getTarget(){
            return _location;
        }
        
        private String _location;
    }

    public Object get( Object n ){
        return null;
    }

    public java.util.Collection<String> keySet(){
        throw new UnsupportedOperationException();
    }

    public boolean containsKey( String s ){
        throw new UnsupportedOperationException();
    }

    public Object set( Object n , Object v ){
        throw new UnsupportedOperationException();
    }

    public Object setInt( int n , Object v ){
        throw new UnsupportedOperationException();
    }

    public Object getInt( int n ){
        throw new UnsupportedOperationException();
    }

    public Object removeField( Object n ){
        throw new UnsupportedOperationException();
    }

    public JSFunction getConstructor(){
        return _mycons;
    }
    
    public void setMessage( Object msg ){
        _msg = msg;
    }

    public String toString(){
        if ( _msg != null )
            return _msg.toString();
        if ( _object != null )
            return _object.toString();
        return "";
    }

    public JSObject getSuper(){
        return null;
    }

    JSFunction _mycons;
    Object _msg;
    final Object _object;
    final boolean _wantedJSException;

}
