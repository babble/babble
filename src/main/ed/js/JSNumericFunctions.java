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

        if ( o == null )
            return o;

        if ( o instanceof String )
            return new JSString( o.toString() );

        if ( o instanceof Number ){
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
    public Object JS_mul( Object a , Object b ){
        a = _parseNumber( a );
        b = _parseNumber( b );

        if ( a != null && ( a instanceof Number ) &&
             b != null && ( b instanceof Number ) ){

            Number an = (Number)a;
            Number bn = (Number)b;

            if ( an instanceof Integer &&
                 bn instanceof Integer ){

                int ai = an.intValue();
                int bi = bn.intValue();

                int mul = an.intValue() * bn.intValue();

                if ( ai > 0 && bi > 0 && mul < 0 );
                else return mul;
            }

            return an.doubleValue() * bn.doubleValue();
        }

        return Double.NaN;
    }

    /** Divides one object by another.
     * @param a First object
     * @param b Second object
     * @return their quotient, if <tt>a</tt> and <tt>b</tt> can be converted to numbers.
     */
    public Object JS_div( Object a , Object b ){
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
    public Object JS_sub( Object a , Object b ){
        a = _parseNumber( a );
        b = _parseNumber( b );

        if ( a != null && ( a instanceof Number ) &&
             b != null && ( b instanceof Number ) ){

            Number an = (Number)a;
            Number bn = (Number)b;

            if ( an instanceof Integer &&
                 bn instanceof Integer )
                return an.intValue() - bn.intValue();

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

        String s1 = JS_toString( a );
        String s2 = JS_toString( b );

        return new JSString( s1 + s2 );
    }

    /** Performs a bitwise or on two objects.
     * @param a First object
     * @param b Second object
     * @return their or-ed value, if <tt>a</tt> and <tt>b</tt> can be converted to numbers.
     */
    public static Number JS_bitor( Object a , Object b ){

        a = _parseNumber( a );
        b = _parseNumber( b );

        if ( a instanceof Boolean )
            if ( ((Boolean)a) )
                a = 1;

        if ( b instanceof Boolean )
            if ( ((Boolean)b) )
                b = 1;

        if ( a != null && a instanceof Number &&
             b != null && b instanceof Number )
            return ((Number)a).intValue() | ((Number)b).intValue();

        if ( a != null && a instanceof Number )
            return (Number)a;

        if ( b != null && b instanceof Number )
            return (Number)b;

        return 0;
    }

    /** Performs a bitwise and on two objects.
     * @param a First object
     * @param b Second object
     * @return their and-ed value, if <tt>a</tt> and <tt>b</tt> can be converted to numbers.
     */
    public Number JS_bitand( Object a , Object b ){

        a = _parseNumber( a );
        b = _parseNumber( b );

        if ( a != null && a instanceof Number &&
             b != null && b instanceof Number )
            return ((Number)a).intValue() & ((Number)b).intValue();

        return 0;
    }

    /** Performs a bitwise exculsive or on two objects.
     * @param a First object
     * @param b Second object
     * @return their xor-ed value, if <tt>a</tt> and <tt>b</tt> can be converted to numbers.
     */
    public Number JS_bitxor( Object a , Object b ){

        a = _parseNumber( a );
        b = _parseNumber( b );

        if ( a != null && a instanceof Number &&
             b != null && b instanceof Number )
            return ((Number)a).intValue() ^ ((Number)b).intValue();

        if ( a != null && a instanceof Number )
            return (Number)a;

        if ( b != null && b instanceof Number )
            return (Number)b;

        return 0;
    }

    /** Given an object, finds its equivalent modulo a second object.
     * @param a First object
     * @param b Second object
     * @return <tt>a</tt> mod <tt>b</tt>, if <tt>a</tt> and <tt>b</tt> can be converted to numbers.
     */
    public Number JS_mod( Object a , Object b ){
        a = _parseNumber( a );
        b = _parseNumber( b );

        if ( a != null && a instanceof Number &&
             b != null && b instanceof Number )
            return ((Number)a).intValue() % ((Number)b).intValue();

        return Double.NaN;
    }

    /** Performs a left shift on an object a given number of bits.
     * @param a First object
     * @param b Second object
     * @return the object's left-shifted value, if <tt>a</tt> and <tt>b</tt> can be converted to numbers.
     */
    public Number JS_lsh( Object a , Object b ){
        a = _parseNumber( a );
        b = _parseNumber( b );

        if ( a != null && a instanceof Number &&
             b != null && b instanceof Number )
            return ((Number)a).intValue() << ((Number)b).intValue();

        if ( a == null || ! ( a instanceof Number ) )
            return 0;

        return (Number)a;
    }

    /** Performs a right shift on an object a given number of bits.
     * @param a First object
     * @param b Second object
     * @return the object's right-shifted value, if <tt>a</tt> and <tt>b</tt> can be converted to numbers.
     */
    public Number JS_rsh( Object a , Object b ){
        a = _parseNumber( a );
        b = _parseNumber( b );

        if ( a != null && a instanceof Number &&
             b != null && b instanceof Number )
            return ((Number)a).intValue() >> ((Number)b).intValue();

        if ( a == null || ! ( a instanceof Number ) )
            return 0;

        return (Number)a;
    }

    /** Performs a zero-propagating right shift on an object a given number of bits.
     * @param a First object
     * @param b Second object
     * @return the object's right-shifted value, if <tt>a</tt> and <tt>b</tt> can be converted to numbers.
     */
    public Number JS_ursh( Object a , Object b ){
        a = _parseNumber( a );
        b = _parseNumber( b );

        if ( a != null && a instanceof Number &&
             b != null && b instanceof Number )
            return ((Number)a).intValue() >>> ((Number)b).intValue();

        if ( a == null || ! ( a instanceof Number ) )
            return 0;

        return (Number)a;
    }

    /** Performs a bitwise not on two objects.
     * @param a First object
     * @param b Second object
     * @return their not-ed value, if <tt>a</tt> and <tt>b</tt> can be converted to numbers.
     */
    public Number JS_bitnot( Object a ){
        a = _parseNumber( a );
        if ( a instanceof Number )
            return ~((Number)a).intValue();
        return -1;
    }

    /** Returns the string representation of an object
     * @param o The object to return
     * @return The object as a string
     */
    public static String JS_toString( Object o ){

        if ( o == null )
            return "null";

        if ( o instanceof Float || o instanceof Double )
            o = fixType( o );

        return o.toString();
    }

    /** @unexpose */
    static final Object _parseNumber( final Object orig ){
        Object o = orig;
        if ( o == null )
            return null;
        
        if ( o instanceof Number )
            return o;

        if ( o instanceof JSDate )
            return ((JSDate)o).getTime();

        String s = null;
        if ( o instanceof JSString )
            s = o.toString();
        else if ( o instanceof String )
            s = o.toString();
        
        if ( s == null )
            return orig;

        if ( s.length() == 0 || s.length() > 9 )
            return 0;

        boolean allDigits = true;
        for ( int i=0; i<s.length(); i++ ){
            final char c = s.charAt( i );
            if ( ! Character.isDigit( c ) ){
                allDigits = false;
                if ( c != '.' )
                    return orig;
            }
        }

        if ( allDigits )
            return Integer.parseInt( s );

        if ( s.matches( "\\d+\\.\\d+" ) )
            return Double.parseDouble( s );

        return orig;
    }
}
