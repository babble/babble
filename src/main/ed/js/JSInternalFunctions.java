// JSInternalFunctions.java

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

import java.util.*;

import org.mozilla.javascript.*;

import ed.js.engine.*;
import ed.js.func.*;

/** @expose */
public class JSInternalFunctions extends JSNumericFunctions {

    static { JS._debugSIStart( "JSInternalFunctions" ); }

    static { JS._debugSI( "JSInternalFunctions" , "0" ); }

    /** String type description: "string" */
    public final static JSString TYPE_STRING = new JSString( "string" );
    static { JS._debugSI( "JSInternalFunctions" , "0.1" ); }
    /** Native string type description: "native_string" */
    public final static JSString TYPE_NATIVE_STRING = new JSString( "native_string" );
    /** Number type description: "number" */
    public final static JSString TYPE_NUMBER = new JSString( "number" );
    /** Boolean type description: "boolean" */
    public final static JSString TYPE_BOOLEAN = new JSString( "boolean" );
    /** Undefined type description: "undefined" */
    public final static JSString TYPE_UNDEFINED = new JSString( "undefined" );
    /** Object type description: "object" */
    public final static JSString TYPE_OBJECT = new JSString( "object" );
    /** Native type description: "native" */
    public final static JSString TYPE_NATIVE = new JSString( "native" );
    /** Functione type description: "function" */
    public final static JSString TYPE_FUNCTION = new JSString( "function" );
    /** Object id type description: "objectid" */
    public final static JSString TYPE_OBJECTID = new JSString( "objectid" );

    static {
        JS._debugSI( "JSInternalFunctions" , "1" );
    }

    /** Function call. */
    public static class FunctionCons extends JSFunctionCalls0 {

            public Object call( Scope s , Object extra[] ){
                Object t = s.getThis();
                if ( t != null )
                    return t;

                return new JSFunctionCalls0(){
                    public Object call( Scope s , Object extra[] ){
                        return null;
                    }
                };
                
            }

            protected void init(){
                JSFunction._init( this );
            }

        };
    
    private static FunctionCons _defaultFunctionCons = new FunctionCons();

    static {
        JS._debugSI( "JSInternalFunctions" , "2" );
    }

    /** Initialize a new set of internal functions. */
    public JSInternalFunctions(){
        super( Scope.getThreadLocalFunction( "Function" , _defaultFunctionCons ) );
    }

    /** Returns the given object
     * @param o Object to return
     * @return <tt>o</tt>
     */
    public static final Object self( Object o ){
        return o;
    }

    /** Checks if an object's constructor is of a given type.
     * @param thing Object to check
     * @param type Type of object
     * @return If the object's type matches the given type
     */
    public static boolean JS_instanceof( Object thing , Object type ){
        if ( thing == null )
            return false;

        if ( type == null )
            throw new NullPointerException( "type can't be null" );

        if ( thing instanceof Number ){
            return false;
        }

        if ( type instanceof String || type instanceof JSString ){
            try {
                Class clazz = JSBuiltInFunctions._getClass( type.toString() );
                return clazz.isAssignableFrom( thing.getClass() );
            }
            catch ( Exception e ){
                throw new JSException( "can't find : " + type , e );
            }
        }

        if ( ! ( thing instanceof JSObject ) )
            return false;

        if ( type instanceof JSBuiltInFunctions.NewObject )
            return true;

        JSObject o = (JSObject)thing;

        if ( o.getConstructor() == null )
            return false;

        return o.getConstructor() == type;
    }

    /** Returns the type of a given object.
     * @param obj Object to determine the type of
     * @return The type
     */
    public JSString JS_typeof( Object obj ){

        if ( obj == null )
            return TYPE_UNDEFINED;

        if ( obj instanceof JSString )
            return TYPE_STRING;

        if ( obj instanceof String )
            return TYPE_NATIVE_STRING;

        if ( obj instanceof Number )
            return TYPE_NUMBER;

        if ( obj instanceof Boolean )
            return TYPE_BOOLEAN;

        if ( obj instanceof JSFunction )
            return TYPE_FUNCTION;

        if ( obj instanceof ed.db.ObjectId )
            return TYPE_OBJECTID;

        if ( obj instanceof JSObject )
            return TYPE_OBJECT;


        return TYPE_NATIVE;
    }

    /** Creates a new object given key/value pairs.
     * @param names Field names to use
     * @param fields Field values corresponding to names
     * @return The constructed object
     */
    public JSObject JS_buildLiteralObject( String names[] , Object ... fields ){
        JSObject o = new JSObjectBase();
        int max = names.length;
        if ( fields != null )
            max = Math.min( fields.length , max );
        for ( int i=0; i<max; i++ )
            o.set( names[i] , fields == null ? null : fields[i] );
        return o;
    }

    /** Perform an arithmetic operation on a subobject and another object and replaces the subobject with the result of the operation.
     * @param obj Object from which to get subobject
     * @param place Field of obj to get
     * @param other Other object to use in operation
     * @param type Type of operation
     * @return The result of the operation
     * @throws JSException If type is invalid
     */
    public Object JS_setDefferedOp( JSObject obj , Object place , Object other , int type ){
        Object nv = null;
        switch ( type ){
        case Token.ADD: nv = JS_add( obj.get( place ) , other ); break;
        case Token.SUB: nv = JS_sub( obj.get( place ) , other ); break;
        case Token.MUL: nv = JS_mul( obj.get( place ) , other ); break;
        case Token.DIV: nv = JS_div( obj.get( place ) , other ); break;
        default: throw new JSException( "unknown op" );
        }
        return obj.set( place , nv );
    }

    /** Add an amount to the key of object's key/value pair.
     * @param ref The key/value pair to use the key of
     * @param post If the key or the value should be returned
     * @param num The amount to be added to the key
     * @return Either the key or the value, depending on the value of <tt>post</tt>
     * @throws RuntimeException If the key is not a number
     */
    public Object JS_inc( JSRef ref , boolean post , int num ){
        Object obj = ref.get();
        if ( obj == null || ! ( obj instanceof Number ) )
            throw new RuntimeException( "got a non number : [" + obj + "] from : [" + ref  + "] is a [" +  ( obj == null ? "null" : obj.getClass().getName() ) + "]" );
        Object n = JS_add( num , obj );
        ref.set( n );
        return post ? obj : n;
    }

    /** Logical or of the boolean values of two objects
     * @param a First object
     * @param b Second object
     * @return If the boolean equivalent of <tt>a</tt> is true, <tt>a</tt>, otherwise <tt>b</tt>
     */
    public static Object JS_or( Object a , Object b ){
        if ( JS_evalToBool( a ) )
            return a;
        return b;
    }

    /** Logical not of the boolean value of a given object.
     * @param o
     * @return The inverse of the boolean equivalent of <tt>o</tt>
     */
    public static Boolean JS_not( Object o ){
        return ! JS_evalToBool( o );
    }

    /** Finds the boolean equivalent of an object.
     * If the object is a number, non-zero and non-NaN values are true.
     * If the object is a string with non-zero length, return true.
     * @param foo
     * @return The boolean equivalent.
     */
    public static boolean JS_evalToBool( Object foo ){
        if ( foo == null )
            return false;

        if ( foo instanceof Boolean )
            return (Boolean)foo;

        if ( foo instanceof Number ){
            Number n = (Number)foo;
            if ( Double.isNaN( n.doubleValue() ) )
                return false;
            return n.doubleValue() != 0;
        }

        if ( foo instanceof String ||
             foo instanceof JSString )
            return foo.toString().length() > 0 ;

        return true;
    }

    /** Checks if one object does not equal another object
     * @param a First object
     * @param b Second object
     * @return If <tt>a</tt> is not equal to <tt>b</tt>
     */
    public static Boolean JS_shne( Object a , Object b ){
        return ! JS_sheq( a , b );
    }

    /** Checks if one object equals another object
     * @param a First object
     * @param b Second object
     * @return If <tt>a</tt> is equal to <tt>b</tt>
     */
    public static Boolean JS_sheq( Object a , Object b ){
        if ( a == b )
            return true;

        if ( a == null && b == null )
            return true;

        if ( a == null || b == null )
            return false;

        if ( a instanceof Number || b instanceof Number ){
            a = _parseNumber( a );
            b = _parseNumber( b );
        }

        if ( a instanceof Number && b instanceof Number ){
            return ((Number)a).doubleValue() == ((Number)b).doubleValue();
        }

        if ( a instanceof JSString )
            a = a.toString();

        if ( b instanceof JSString )
            b = b.toString();

        if ( a instanceof String && b instanceof String )
            return a.equals( b );

        if ( _charEQ( a , b ) || _charEQ( b , a ) )
            return true;

        return false;
    }

    /** @unexpose */
    private static boolean _charEQ( Object a , Object b ){
        if ( ! ( a instanceof String ) )
            return false;

        if ( ! ( b instanceof Character ) )
            return false;

        String s = (String)a;
        Character c = (Character)b;

        if ( s.length() != 1 )
            return false;

        return s.charAt(0) == c;
    }

    /** Checks if one object equals another object using their types and, if that fails, their equals methods.
     * @param a First object
     * @param b Second object
     * @return If <tt>a</tt> is equal to <tt>b</tt>
     */
    public static Boolean JS_eq( Object a , Object b ){
        if ( JS_sheq( a , b ) )
            return true;

	if ( a == null || b == null )
	    return false;

	return a.equals( b );
    }

    /** Checks if one object is greater than or equal to another object.
     * @param a First object
     * @param b Second object
     * @return If <tt>a</tt> is greater than or equal to <tt>b</tt>
     */
    public Boolean JS_ge( Object a , Object b ){
        return _compare( a , b ) >= 0;
    }

    /** Checks if one object is less than or equal to another object.
     * @param a First object
     * @param b Second object
     * @return If <tt>a</tt> is less than or equal to <tt>b</tt>
     */
    public Boolean JS_le( Object a , Object b ){
        return _compare( a , b ) <= 0;
    }

    /** Checks if one object is less than another object.
     * @param a First object
     * @param b Second object
     * @return If <tt>a</tt> is less than <tt>b</tt>
     */
    public Boolean JS_lt( Object a , Object b ){
        return _compare( a , b ) < 0;
    }

    /** Checks if one object is greater than another object.
     * @param a First object
     * @param b Second object
     * @return If <tt>a</tt> is greater than <tt>b</tt>
     */
    public Boolean JS_gt( Object a , Object b ){
        return _compare( a , b ) > 0;
    }

    /** @unexpose */
    int _compare( Object a , Object b ){
        if ( a == null && b == null )
            return 0;

        if ( a == null ){
            if ( b instanceof Number )
                a = 0;
            else
                return 1;
        }

        if (  b == null ){
            if ( a instanceof Number )
                b = 0;
            else
                return -1;
        }

        if ( a.equals( b ) )
            return 0;

        if ( a instanceof Number || b instanceof Number ){
            a = _parseNumber( a );
            b = _parseNumber( b );
        }

        if ( a instanceof Number &&
             b instanceof Number ){
            double aVal = ((Number)a).doubleValue();
            double bVal = ((Number)b).doubleValue();
            if ( aVal == bVal )
                return 0;
            return aVal < bVal ? -1 : 1;
        }

        if ( a instanceof Comparable )
            return ((Comparable)a).compareTo( b );

        return a.toString().compareTo( b.toString() );
    }

    /** If <tt>o</tt> is a number, parses and return it, otherwise parses and returns <tt>def</tt>.
     * @param o First object to parse
     * @param def Second object to parse
     * @return The parsed number.
     */
    public static final Object parseNumber( final Object o , final Object def ){
	Object r = _parseNumber( o );
	if ( r instanceof Number )
	    return r;
	return _parseNumber( def );
    }

    /** Returns the keyset that can be iterated over for a given object.
     * @param o Object from which to get keyset
     * @return The keyset
     * @throws RuntimeException If <tt>o</tt> exists but doesn't have a keyset
     */
    public static final Collection<String> JS_collForFor( Object o ){
        if ( o == null )
            return new LinkedList<String>();

        if ( o instanceof JSObject )
            return ((JSObject)o).keySet();

        if ( o instanceof Collection )
            return (Collection<String>)o;

        throw new RuntimeException( "can't for with a : " + o.getClass() );
    }

    /** Returns the last argument passed.
     * @param o Some number of objects
     * @return The last object passed in
     */
    public static final Object JS_comma( Object ... o ){
        if ( o == null || o.length == 0 )
            return null;
        return o[o.length-1];
    }

    /** The hash code for a given object.
     * @param o Object for which to create a hashcode
     * @return The hashcode
     */
    public static long hash( Object o ){
        long hash = 0;

        if ( o == null )
            return hash;

        if ( o instanceof Collection ){
            for ( Object foo : (Collection)o )
                hash += hash( o );
        }
        else if ( o.getClass().isArray() ){
            Object a[] = (Object[])o;
            for ( int i=0; i<a.length; i++ )
                hash += hash( a[i] );
        }
        else {
            hash += o.hashCode();
        }

        return hash;
    }

    /** @unexpose */
    static String _debug( Object o ){
        if ( o == null )
            return "null";

        return "[" + o + "](" + o.getClass() + ")" ;
    }

    static {
        JS._debugSIDone( "JSInternalFunctions" );
    }
}
