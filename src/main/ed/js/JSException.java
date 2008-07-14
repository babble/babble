// JSException.java

package ed.js;

import ed.js.func.*;
import ed.js.engine.*;

/** @expose */
public class JSException extends RuntimeException implements JSObject {

    /** @unexpose */
    public final static JSFunction _quietCons = new JSFunctionCalls1(){

            public JSObject newOne(){
                return new Quiet( "quiet" );
            }

            public Object call( Scope scope , Object msg , Object[] extra ){
                return new Quiet( msg );
            }

            protected void init(){

            }
        };


    /** @unexpose */
    public final static JSFunction _redirectCons = new JSFunctionCalls1(){
            public JSObject newOne(){
                return new Quiet( "redirect" );
            }
            public Object call( Scope scope , Object to , Object[] extra){
                return new Redirect( to );
            }

            protected void init(){

            }
        };

    public static class cons extends JSFunctionCalls1{

        public JSObject newOne(){
            return new JSException( "no msg set yet" );
        }

        public Object call( Scope scope , Object msg , Object[] extra ){
            Object foo = scope.getThis();

            if ( foo == null || ! ( foo instanceof JSException ) )
                return new JSException( msg , null , true );

            JSException e = (JSException)foo;
            e._msg = msg;
            e._object = msg;
            return e;
        }

        protected void init(){
            set( "Quiet" , _quietCons );
            set( "Redirect" , _redirectCons );
        }
    }

    /** @unexpose */
    public final static JSFunction _cons = new cons();

    /** Initializes a new exception
     * @param o Object describing the exception
     */
    public JSException( Object o ){
        this( o , null );
    }

    /** Initializes a new exception
     * @param o Object describing the exception
     * @param t Error or exception being thrown
     */
    public JSException( Object o , Throwable t ){
        this( o , t , false );
    }

    /** Initializes a new exception
     * @param o Object describing the exception
     * @param t Error or exception being thrown
     * @param wantedJSException
     */
    public JSException( Object o , Throwable t , boolean wantedJSException ){
        super( ( o == null ? "" : o.toString() ).toString() , _fix( t ) );
        _object = o;
        _wantedJSException = wantedJSException;
    }

    /** Returns the description of the exception or the whole exception.
     * @return If wantedJSException was set to <tt>true</tt> in the initializer, this exception is returned.
     * Otherwise, the object describing the exception is returned.
     */
    public Object getObject(){
        if ( _wantedJSException )
            return this;
        return _object;
    }

    /** @unexpose */
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

    /** @unexpose */
    public Object get( Object n ){
        return null;
    }

    /** @unexpose */
    public java.util.Collection<String> keySet(){
        throw new UnsupportedOperationException();
    }

    /** @unexpose */
    public boolean containsKey( String s ){
        throw new UnsupportedOperationException();
    }

    /** @unexpose */
    public Object set( Object n , Object v ){
        throw new UnsupportedOperationException();
    }

    /** @unexpose */
    public Object setInt( int n , Object v ){
        throw new UnsupportedOperationException();
    }

    /** @unexpose */
    public Object getInt( int n ){
        throw new UnsupportedOperationException();
    }

    /** @unexpose */
    public Object removeField( Object n ){
        throw new UnsupportedOperationException();
    }

    /** Returns the constructor for this exception.
     * @return Could be redirect or quiet constructors.
     */
    public JSFunction getConstructor(){
        return _mycons;
    }

    /** Set this exception's message.
     * @param msg The message to set.
     */
    public void setMessage( Object msg ){
        _msg = msg;
    }

    /** Returns the stringified message or, if that doesn't exist, the stringified object describing this exception.
     * @return This exception in string form.
     */
    public String toString(){
        if ( _msg != null )
            return _msg.toString();
        if ( _object != null )
            return _object.toString();
        return "";
    }

    /** @unexpose */
    public JSObject getSuper(){
        return null;
    }

    /** @unexpose */
    JSFunction _mycons;
    private Object _msg;
    private Object _object;
    private boolean _wantedJSException;

}
