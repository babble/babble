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
import java.io.IOException;

import ed.ext.org.mozilla.javascript.*;

import ed.js.engine.*;
import ed.js.func.*;
import ed.js.e4x.*;
import ed.util.*;

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
    /** XML object description */
    public final static JSString TYPE_XML = new JSString( "xml" );

    static {
        JS._debugSI( "JSInternalFunctions" , "1" );
    }

    /** Function call. */
    public static class FunctionCons extends JSFunctionCalls1 {
        
        public JSObject newOne() {
            return new JSFunctionCalls0() {
                public Object call( Scope s , Object extra[] ) {
                    return null;
                }
            };
        }

        public Object call( Scope s , Object foo , Object extra[] ) {
            // Function()
            if( foo == null && extra == null ) {
                return newOne();
            }

            String precode = foo == null ? "return null" : foo.toString();
            if( extra.length > 0 ) {
                precode = extra[ extra.length - 1 ].toString();
            }
            final String code = precode;

            // parse the function's args
            // remember, foo is the first extra
            String argList[];
            if( extra.length > 0 ) {
                String argsStr = foo.toString();
                for( int i=0; i<extra.length-1; i++) {
                    argsStr += "," + extra[i];
                }
                argList = argsStr.split( "," );
            }
            else {
                argList = new String[0];
            }

            // set the function length manually
            JSFunction func =  new JSFunctionCalls0( argList.length ){
                    public Object call( Scope s2 , Object extra2[] ){
                        Scope local = s2.child();
                        // get any named arguments
                        if( this._arguments != null ) {
                            int namedArgs = Math.min( this._arguments.size(), extra2.length );
                            for( int i=0; i < namedArgs; i++ ) {
                                local.set( this._arguments.get( i ).toString(), extra2[i] );
                            }
                        }

                        // eval doesn't take arguments, so we'll do the conversion ourselves
                        try {
                            String randomFile = "anon"+Math.random();
                            Convert c = new Convert( randomFile , code , true);
                            return c.get().call( local, extra2 );
                        }
                        catch( IOException e ) {
                            System.out.println("couldn't convert "+code+":");
                            e.printStackTrace();
                        }
                        return null;
                    }
                };

            if( extra.length > 0 ) {
                func._arguments = new JSArray();
                for( String arg : argList ) {
                    func._arguments.add( arg );
                }
            }

            Object o = s.getThis();
            if( o instanceof JSFunction ) {
                s.clearThisNormal( o ); // get rid of the empty function
                s.setThis( func ); // put func with code in it on the scope stack
            }

            return func;
        }

        protected void init(){
            JSFunction._init( this );
            setProperties( "prototype", JSObjectBase.LOCK );
        }

    };

    private static FunctionCons _defaultFunctionCons = new FunctionCons();

    static JSFunction getFunctionCons(){
        return Scope.getThreadLocalFunction( "Function" , _defaultFunctionCons );
    }

    static {
        JS._debugSI( "JSInternalFunctions" , "2" );
    }

    /** Initialize a new set of internal functions. */
    public JSInternalFunctions(){
        super( getFunctionCons() );
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

        if ( obj instanceof ENode )
            return TYPE_XML;

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
        Object n;
        if ( ! ( obj instanceof Number ) ) {
            obj = JSNumber.getDouble( obj );
            n = (Double)obj + num;
            ref.set( n );
        }
        else {
            n = JS_add( num , obj );
            ref.set( n );
        }
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

    /** Checks if one object does not equal another object exactly (!==)
     * @param a First object
     * @param b Second object
     * @return If <tt>a</tt> is not equal to <tt>b</tt>
     */
    public static Boolean JS_shne( Object a , Object b ){
        return ! JS_sheq( a , b );
    }

    /** Checks if one object does not equal another object (!=)
     * @param a First object
     * @param b Second object
     * @return If <tt>a</tt> is not equal to <tt>b</tt>
     */
    public static Boolean JS_ne( Object a , Object b ){
        return ! JS_eq( a , b );
    }

    /** Checks if one object equals another object
     * @param a First object
     * @param b Second object
     * @return If <tt>a</tt> is equal to <tt>b</tt>
     */
    public static Boolean JS_sheq( Object a , Object b ){
        if ( a == null && b == null )
            return true;

        if ( a == null || b == null )
            return false;

        if ( a instanceof Boolean && b instanceof Boolean ) {
            return a.equals( b );
        }

        if ( a instanceof Number || b instanceof Number ){
            a = _parseNumber( a );
            b = _parseNumber( b );
        }

        if ( a instanceof Number && b instanceof Number ){
            if( Double.isNaN( ((Number)a).doubleValue() ) ||
                Double.isNaN( ((Number)b).doubleValue() ) ) {
                return false;
            }
            return ((Number)a).doubleValue() == ((Number)b).doubleValue();
        }
        
        if ( a instanceof JSString && b instanceof JSString )
            return a.equals( b );

        if ( a instanceof String && b instanceof String )
            return a.equals( b );

        if ( _charEQ( a , b ) || _charEQ( b , a ) )
            return true;

        return a == b;
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

        if( a instanceof ENode ) { 
            a = a.toString();
        }
        if( b instanceof ENode ) { 
            b = b.toString();
        }

        if ( a instanceof JSObject &&
             b instanceof JSObject ) {
            return a.equals( b );
        }

        if( a instanceof JSString ) 
            a = a.toString();
        if( b instanceof JSString ) 
            b = b.toString();

        // "promote" booleans and strings to numbers
        if ( a instanceof JSBoolean || a instanceof Boolean ) {
            a = JSBoolean.booleanValue( a ) ? 1 : 0;
        }
        if ( b instanceof JSBoolean || b instanceof Boolean ) {
            b = JSBoolean.booleanValue( b ) ? 1 : 0;
        }
        if ( a instanceof Number && 
             ( b instanceof String ) && 
             b.toString().matches( JSNumber.POSSIBLE_NUM ) ) {
            a = ((Number)a).doubleValue();
            b = StringParseUtil.parseStrict(b.toString()).doubleValue();
        }
        if ( b instanceof Number && 
             ( a instanceof String ) &&
             a.toString().matches( JSNumber.POSSIBLE_NUM ) ) {
            a = StringParseUtil.parseStrict(a.toString()).doubleValue();
            b = ((Number)b).doubleValue();
        }

        if ( ( a instanceof Number && Double.isNaN( ((Number)a).doubleValue() ) ) ||
             ( b instanceof Number && Double.isNaN( ((Number)b).doubleValue() ) ) )
            return false;

	return a.equals( b );
    }

    /** Checks if one object is greater than or equal to another object.
     * @param a First object
     * @param b Second object
     * @return If <tt>a</tt> is greater than or equal to <tt>b</tt>
     */
    public static Boolean JS_ge( Object a , Object b ){
        return _compare( a , b ) >= 0;
    }

    /** Checks if one object is less than or equal to another object.
     * @param a First object
     * @param b Second object
     * @return If <tt>a</tt> is less than or equal to <tt>b</tt>
     */
    public static Boolean JS_le( Object a , Object b ){
        return _compare( a , b ) <= 0;
    }

    /** Checks if one object is less than another object.
     * @param a First object
     * @param b Second object
     * @return If <tt>a</tt> is less than <tt>b</tt>
     */
    public static Boolean JS_lt( Object a , Object b ){
        return _compare( a , b ) < 0;
    }

    /** Checks if one object is greater than another object.
     * @param a First object
     * @param b Second object
     * @return If <tt>a</tt> is greater than <tt>b</tt>
     */
    public static Boolean JS_gt( Object a , Object b ){
        return _compare( a , b ) > 0;
    }
    
    /** @unexpose */
    final static int _compare( Object a , Object b ){
        if ( a == null ){
            if ( b == null )
                return 0;
            if ( b instanceof Number )
                return _compare( 0 , (Number)b );
            return 1;
        }
        
        if (  b == null ){
            if ( a == null )
                return 0;
            if ( a instanceof Number )
                return _compare( (Number)a , 0 );
            return -1;
        }
        
        if ( a instanceof Number ){
            b = _parseNumber( b );
            if ( b instanceof Number )
                return _compare( (Number)a , (Number)b );
        }
        else if ( b instanceof Number ){
            a = _parseNumber( a );
            if ( a instanceof Number )
                return _compare( (Number)a , (Number)b );
        }

        if ( a instanceof Comparable )
            return ((Comparable)a).compareTo( b );

        if ( a.equals( b ) )
            return 0;

        return a.toString().compareTo( b.toString() );
    }

    final static int _compare( final Number a , final Number b ){
        final double diff = a.doubleValue() - b.doubleValue();
        if ( diff == 0 )
            return 0;
        
        if ( diff < 0 )
            return -1;

        return 1;
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

        if ( o instanceof Number )
            return EMPTY_STRING_LIST;

        throw new RuntimeException( "can't for with a : " + o.getClass() );
    }

    /** Returns the value set that can be iterated over for a given object.
     * @param o Object from which to get values
     * @return The values
     * @throws RuntimeException If <tt>o</tt> exists but doesn't have values
     */
    public static final Collection JS_collForForEach( Object o ){
        if ( o == null )
            return new LinkedList();

        if ( o instanceof ENode )
            return ((ENode)o).valueSet();
        
        if ( o instanceof Collection )
            return (Collection)o;

        if ( o instanceof JSObject ){
            JSObject j = (JSObject)o;
            Collection<String> keys = j.keySet();
            List values = new ArrayList( keys.size() );
            for ( String k : keys )
                values.add( j.get( k ) );
            return values;
        }

        throw new RuntimeException( "can't for each with a : " + o.getClass() );
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

    public final static JSObject JS_toJSObject( final Object o ){
        if ( o == null )
            return null;
        
        if ( o instanceof JSObject )
            return (JSObject)o;
        
        if ( o instanceof Number )
            return new JSNumber( (Number)o );
        
        throw new RuntimeException( "can't convert [" + o.getClass() + "] to a JSObject" );
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
    
    static final List<String> EMPTY_STRING_LIST = Collections.synchronizedList( new LinkedList<String>() );
}
