// JSException.java

/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package ed.js;

import ed.js.func.*;
import ed.js.engine.*;

/** @expose */
public class JSException extends RuntimeException implements JSObject {

    /** @unexpose */
    static class quietCons extends JSFunctionCalls1{

            public JSObject newOne(){
                return new Quiet( "quiet" , this );
            }

            public Object call( Scope scope , Object msg , Object[] extra ){
                return new Quiet( msg , this );
            }

            protected void init(){

            }
        };


    /** @unexpose */
    static class redirectCons extends JSFunctionCalls1{
            public JSObject newOne(){
                return new Quiet( "redirect" , this );
            }
            public Object call( Scope scope , Object to , Object[] extra){
                return new Redirect( to , this );
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
            set( "Quiet" , new quietCons() );
            set( "Redirect" , new redirectCons() );
        }
    }

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
        super( _toString( o ) , _fix( t ) );
        _object = o;
        _wantedJSException = wantedJSException;
    }

    static String _toString( Object o ){
        if ( o == null )
            return "";
        
        if ( o instanceof JSObjectBase )
            return ((JSObjectBase)o).toPrettyString();

        return o.toString();
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
        public Quiet( Object msg , JSFunction cons){
            super( msg , null , true );
            _mycons = cons;
        }
    }

    public static class Redirect extends JSException {
        public Redirect( Object location , JSFunction cons ){
            super( "redirecting" , null , true );
            _mycons = cons;

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

    public java.util.Collection<String> keySet( boolean includePrototype ){
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
            return _toString( _msg );
        if ( _object != null )
            return _toString( _object );
        return "";
    }

    /** @unexpose */
    public JSObject getSuper(){
        return null;
    }

    public JSFunction getFunction( String name ){
        return JSObjectBase.getFunction( this , name );
    }

    /** @unexpose */
    JSFunction _mycons;
    private Object _msg;
    private Object _object;
    private boolean _wantedJSException;

}
