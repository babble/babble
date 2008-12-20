// JSNumericFunctions.java

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

import ed.js.e4x.*;
import ed.util.StringParseUtil;

/** @expose
 */
public class JSNumericFunctions extends JSObjectBase {

    static {
        JS._debugSIStart( "JSNumericFunctions" );
    }

    /** Initializes this numeric function
     * @param cons Function to initialize
     */
    JSNumericFunctions( JSFunction cons ){
        super( cons );
    }

    /** Finds the type of a given object.
     * @param o
     * @return The object, possibly converted to an equivalent JS type
     */
    public static final Object fixType( final Object o ){
	return fixType( o , true );
    }

    public static final Object fixType( final Object o , final boolean numbers ){

        if ( o == null )
            return o;

        if ( o instanceof String )
            return new JSString( o.toString() );

        if ( numbers && o instanceof Number ){
            if ( o instanceof Float ||
                 o instanceof Double ){

                final Number n = (Number)o;
                final double d = n.doubleValue();

                if ( couldBeInt( d ) )
                    return n.intValue();

                if ( couldBeLong( d ) )
                    return (long)d;
            }

            return o;
        }


        return o;
    }

    /** Returns if the given number could be an int value
     * @param d The given number
     * @return if the given number could be an int value
     */
    public static final boolean couldBeInt( final double d ){
        if ( d > Integer.MAX_VALUE / 2 )
            return false;

        if ( d < Integer.MIN_VALUE / 2 )
            return false;

        if ( Math.floor( d ) != d )
            return false;

        return true;
    }

    /** Returns if the given number could be a long int value
     * @param d The given number
     * @return if the given number could be a long int value
     */
    public static final boolean couldBeLong( final double d ){
        if ( d > Long.MAX_VALUE / 2 )
            return false;

        if ( d < Long.MIN_VALUE / 2 )
            return false;

        long l = (long)d;
        return l == d;
    }

    /** Multiplies two objects.
     * @param a First object
     * @param b Second object
     * @return their product, if <tt>a</tt> and <tt>b</tt> can be converted to numbers.
     */
    public Double JS_mul( Object a , Object b ){
        a = _parseNumber( a );
        b = _parseNumber( b );

        if ( a != null && ( a instanceof Number ) &&
             b != null && ( b instanceof Number ) ){

            Number an = (Number)a;
            Number bn = (Number)b;
	    /*
            if ( an instanceof Integer &&
                 bn instanceof Integer ){

                int ai = an.intValue();
                int bi = bn.intValue();

                int mul = an.intValue() * bn.intValue();
		
                if ( ai > 0 && bi > 0 && mul < 0 );
                else return (double)mul;
            }
	    */
            return an.doubleValue() * bn.doubleValue();
        }

        return Double.NaN;
    }

    /** Divides one object by another.
     * @param a First object
     * @param b Second object
     * @return their quotient, if <tt>a</tt> and <tt>b</tt> can be converted to numbers.
     */
    public Double JS_div( Object a , Object b ){
        a = _parseNumber( a );
        b = _parseNumber( b ) ;

        if ( a != null && ( a instanceof Number ) &&
             b != null && ( b instanceof Number ) ){

            Number an = (Number)a;
            Number bn = (Number)b;

            return an.doubleValue() / bn.doubleValue();
        }

        return Double.NaN;
    }

    /** Subtracts one object from another.
     * @param a First object
     * @param b Second object
     * @return their difference, if <tt>a</tt> and <tt>b</tt> can be converted to numbers.
     */
    public Double JS_sub( Object a , Object b ){
        a = _parseNumber( a );
        b = _parseNumber( b );

        if ( a != null && ( a instanceof Number ) &&
             b != null && ( b instanceof Number ) ){

            Number an = (Number)a;
            Number bn = (Number)b;
	    /*
            if ( an instanceof Integer &&
                 bn instanceof Integer )
                return an.intValue() - bn.intValue();
	    */
            return an.doubleValue() - bn.doubleValue();
        }

        return Double.NaN;
    }

    /** @unexpose */
    static Object _addParse( Object o ){
        if ( o instanceof JSDate )
            return o.toString();
        return o;
    }

    /** Adds one object to another.
     * @param a First object
     * @param b Second object
     * @return their sum, if <tt>a</tt> and <tt>b</tt> can be converted to numbers.
     */
    public Object JS_add( Object a , Object b ){

        a = _addParse( a );
        b = _addParse( b );

        if ( a != null && ( a instanceof Number ) &&
             b != null && ( b instanceof Number ) ){

            Number an = (Number)a;
            Number bn = (Number)b;

            if ( an instanceof Integer &&
                 bn instanceof Integer )
                return an.intValue() + bn.intValue();
	    
	    if ( ( an instanceof Long && bn instanceof Integer ) || 
		 ( an instanceof Integer && bn instanceof Long ) )
		return an.longValue() + bn.longValue();

            return an.doubleValue() + bn.doubleValue();
        }

        if ( ( a != null && ( a instanceof Number ) && b == null ) ||
             ( b != null && ( b instanceof Number ) && a == null ) ){
            return Double.NaN;
        }

        if( a instanceof ENode && b instanceof ENode ) {
            XMLList list = E4X.addNodes((ENode)a, (ENode)b);
            return list;
        }

        String s1 = JS_toString( a );
        String s2 = JS_toString( b );

	StringBuilder buf = new StringBuilder( s1.length() + s2.length() + 5 );
	buf.append( s1 ).append( s2 );

        return new JSString( buf.toString() );
    }

    /** Performs a bitwise or on two objects.
     * @param a First object
     * @param b Second object
     * @return their or-ed value, if <tt>a</tt> and <tt>b</tt> can be converted to numbers.
     */
    public static Number JS_bitor( Object a , Object b ){
        return JSNumber.toInt32( a ) | JSNumber.toInt32( b );
    }

    /** Performs a bitwise and on two objects.
     * @param a First object
     * @param b Second object
     * @return their and-ed value, if <tt>a</tt> and <tt>b</tt> can be converted to numbers.
     */
    public Number JS_bitand( Object a , Object b ){
        return JSNumber.toInt32( a ) & JSNumber.toInt32( b );
    }

    /** Performs a bitwise exculsive or on two objects.
     * @param a First object
     * @param b Second object
     * @return their xor-ed value, if <tt>a</tt> and <tt>b</tt> can be converted to numbers.
     */
    public Number JS_bitxor( Object a , Object b ){
        return JSNumber.toInt32( a ) ^ JSNumber.toInt32( b );
    }

    /** Given an object, finds its equivalent modulo a second object.
     * @param a First object
     * @param b Second object
     * @return <tt>a</tt> mod <tt>b</tt>, if <tt>a</tt> and <tt>b</tt> can be converted to numbers.
     */
    public Number JS_mod( Object a , Object b ){
        a = JSNumber.getNumber( a );
        b = JSNumber.getNumber( b );

        if ( Double.isNaN( ((Number)a).doubleValue() ) ||
             Double.isNaN( ((Number)b).doubleValue() ) || 
             Double.isInfinite( ((Number)a).doubleValue() ) ||
             ((Number)b).longValue() == 0 )
            return Double.NaN;

        return ((Number)a).longValue() % ((Number)b).longValue();
    }

    /** Performs a left shift on an object a given number of bits.
     * @param a First object
     * @param b Second object
     * @return the object's left-shifted value, if <tt>a</tt> and <tt>b</tt> can be converted to numbers.
     */
    public Number JS_lsh( Object a , Object b ){
        return JSNumber.toInt32( a ) << JSNumber.toUint32( b );
    }

    /** Performs a right shift on an object a given number of bits.
     * @param a First object
     * @param b Second object
     * @return the object's right-shifted value, if <tt>a</tt> and <tt>b</tt> can be converted to numbers.
     */
    public Number JS_rsh( Object a , Object b ){
        return JSNumber.toInt32( a ) >> JSNumber.toUint32( b );
    }

    /** Performs a zero-propagating right shift on an object a given number of bits.
     * @param a First object
     * @param b Second object
     * @return the object's right-shifted value, if <tt>a</tt> and <tt>b</tt> can be converted to numbers.
     */
    public Number JS_ursh( Object a , Object b ){
        return JSNumber.toInt32( a ) >>> JSNumber.toUint32( b );
    }

    /** Performs a bitwise not on two objects.
     * @param a First object
     * @param b Second object
     * @return their not-ed value, if <tt>a</tt> and <tt>b</tt> can be converted to numbers.
     */
    public Number JS_bitnot( Object a ){
        return ~JSNumber.toInt32( a );
    }

    /** Returns the string representation of an object
     * @param o The object to return
     * @return The object as a string
     */
    public static String JS_toString( Object o ){
        if ( o == JSInternalFunctions.VOID ) 
            return "undefined";

        if ( o instanceof Number ) {
            return JSNumber.formatNumber( (Number)o );
        }

        return o + "";
    }

    /** @unexpose */
    static final Number _parseNumber( final Object orig ){
        Object o = orig;
        if ( o == null )
            return Double.NaN;
        
        if ( o instanceof Number )
            return (Number)o;

        if ( o instanceof JSDate )
            return ((JSDate)o).getTime();

        return JSNumber.getDouble( o );
    }
}
