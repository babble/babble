// JSMath.java

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
import java.util.regex.*;

import ed.util.StringParseUtil;

import ed.js.func.*;
import ed.js.engine.*;

/** @expose
 * @anonymous name : { max }, desc : { Returns the greater of two numbers. }, param : {type : (number), name : (a)} param : {type : (number), name : (b)} return : {type : (number), desc : (whichever number was greater)}
 * @anonymous name : { min }, desc : { Returns the lesser of two numbers. }, param : {type : (number), name : (a)} param : {type : (number), name : (b)} return : {type : (number), desc : (whichever number was smaller)}
 * @anonymous name : { random }, desc : { Returns a double value with a positive sign, greater than or equal to 0.0 and less than 1.0. } return : {type : (number), desc : (a random number between 0 and 1)}
 * @anonymous name : { floor }, desc : { Rounds a given number down. }, param : {type : (number), name : (a)} return : {type : (number)}
 * @anonymous name : { ceil }, desc : { Rounds a given number up. }, param : {type : (number), name : (a)} return : {type : (number)}
 * @anonymous name : { round }, desc : { Rounds a given number. }, param : {type : (number), name : (a)} return : {type : (number)}
 * @anonymous name : { abs }, desc : { Returns the absolute value of a number. },  param : {type : (number), name : (a)}  return : {type : (number), desc : (the absolute value)}
 * @anonymous name : {sqrt}, desc : {Returns the square route of a number.},   param : {type : (number), name : (a)} return : {type : (number), desc : (the square root)}
 * @anonymous name : {posOrNeg}, desc : {Returns if the number is greater, less than, or equal to 0.}, return : {type : (number), desc : (1 if the number is positive, -1 if negative, 0 if the number is zero)},  param : {type : (number), name : (a)}
 * @anonymous name : {sigFig}, desc : {Returns the number with all but the three most significant digits set to 0.},  return : {type : (number)}, param : {type : (number), name : (a), desc : (number from which to drop insignificant digits)}
 */
public class JSMath extends JSObjectBase {

    public JSMath(){
        set( "max" ,
             new JSFunctionCalls2(){
                 public Object call( Scope s , Object foo[] ) {
                     return Double.POSITIVE_INFINITY;
                 }
                 public Object call( Scope s , Object a , Object b , Object foo[] ){
                     double d1 = getDouble( a );
                     double d2 = getDouble( b );
                     if( Double.isNaN( d1 ) || Double.isNaN( d2 ) )
                         return Double.NaN;
                     return d1 > d2 ? d1 : d2;
                 }
             } );

        set( "min" ,
             new JSFunctionCalls2(){
                 public Object call( Scope s , Object foo[] ) {
                     return Double.POSITIVE_INFINITY;
                 }
                 public Object call( Scope s , Object a , Object b , Object foo[] ){
                     double d1 = getDouble( a );
                     double d2 = getDouble( b );
                     if( Double.isNaN( d1 ) || Double.isNaN( d2 ) )
                         return Double.NaN;
                     return d1 < d2 ? d1 : d2;
                 }
             } );

        set( "random" ,
             new JSFunctionCalls0(){
                 public Object call( Scope s , Object foo[] ){
                     return Math.random();
                 }
             } );

        set( "floor" ,
             new JSFunctionCalls1(){
                 public Object call( Scope s , Object foo[] ) {
                     return Double.NaN;
                 }
                 public Object call( Scope s , Object a , Object foo[] ){
                     double d = getDouble( a );
                     if( Double.isNaN( d ) || Double.isInfinite( d ) ) {
                         return d;
                     }
                     return (int)Math.floor( d );
                 }
             } );

        set( "ceil" ,
             new JSFunctionCalls1(){
                 public Object call( Scope s , Object foo[] ) {
                     return Double.NaN;
                 }
                 public Object call( Scope s , Object a , Object foo[] ){
                     double d = getDouble( a );
                     if( Double.isNaN( d ) || Double.isInfinite( d ) ) {
                         return d;
                     }
                     return (int)Math.ceil( d );
                 }
             } );

        set( "round" ,
             new JSFunctionCalls1(){
                 public Object call( Scope s , Object foo[] ) {
                     return Double.NaN;
                 }

                 public Object call( Scope s , Object a, Object foo[] ){
                     if ( a == null )
                         return 0;
                     if ( ! ( a instanceof Number ) ) {
                         if( a instanceof Boolean ) {
                             boolean b = ((Boolean)a).booleanValue();
                             return b ? 1 : 0;
                         }
                         try {
                             a = StringParseUtil.parseStrict(a.toString());
                         }
                         catch (Exception e) {
                             return Double.NaN;
                         }
                     }
                     // Java returns 0 for Math.round(NaN), MIN_VALUE for
                     // Math.round( -inf ), and MAX_VALUE for Math.round( inf )
                     // JavaScript returns:
                     if( a.equals( Double.NaN ) )
                         return Double.NaN;
                     if( a.equals( Double.POSITIVE_INFINITY ) ) 
                         return Double.POSITIVE_INFINITY;
                     if( a.equals( Double.NEGATIVE_INFINITY ) ) 
                         return Double.NEGATIVE_INFINITY;
                     return (int)Math.round(((Number)a).doubleValue());
                 }
             } );

        set( "abs" ,
             new JSFunctionCalls1(){
                 public Object call( Scope s , Object foo[] ){
                     return Double.NaN;
                 }
                 public Object call( Scope s , Object a , Object foo[] ){
                     return Math.abs( getDouble( a ) );
                 }
             } );


        set( "sqrt" ,
             new JSFunctionCalls1(){
                 public Object call( Scope s , Object foo[] ){
                     return Double.NaN;
                 }
                 public Object call( Scope s , Object a , Object foo[] ){
                     return Math.sqrt( getDouble( a ) );
                 }

             } );

        set( "posOrNeg" , new JSFunctionCalls1(){
                public Object call( Scope s , Object a , Object foo[] ){

                    if ( ! ( a instanceof Number ) )
                        return 0;

                    Number n = (Number)a;
                    double d = n.doubleValue();
                    if ( d > 0 )
                        return 1;
                    if ( d < 0 )
                        return -1;
                    return 0;
                }
            } );

        set( "sigFig" , new JSFunctionCalls2(){
                public Object call( Scope s , Object xObject , Object nObject , Object foo[] ){
                    double X = ((Number)xObject).doubleValue();
                    double N = nObject == null ? 3 : ((Number)nObject).doubleValue();
                    return sigFig( X , N );
                }
            } );

        set( "asin" , new TrigFunc( "asin" ) );
        set( "acos" , new TrigFunc( "acos" ) );
        set( "atan" , new TrigFunc( "atan" ) );
        set( "sin" , new TrigFunc( "sin" ) );
        set( "cos" , new TrigFunc( "cos" ) );
        set( "tan" , new TrigFunc( "tan" ) );

        set( "atan2" , new JSFunctionCalls2(){
                public Object call( Scope s , Object foo[] ){
                    return Double.NaN;
                }
                public Object call( Scope s , Object a , Object b , Object foo[] ){
                    double d1 = getDouble( a );
                    double d2 = getDouble( b );
                    return Math.atan2( d1 , d2 );
                }
            } );

        set( "pow" , new JSFunctionCalls2(){
                public Object call( Scope s , Object foo[] ){
                    return Double.NaN;
                }
                public Object call( Scope s , Object baseArg , Object expArg , Object foo[] ){
                    double base = getDouble( baseArg );
                    double exp = getDouble( expArg );
                    return Math.pow(base, exp);
                }
            } );
        set( "exp" , new JSFunctionCalls1(){
                public Object call( Scope s , Object foo[] ){
                    return Double.NaN;
                }
                public Object call( Scope s , Object expArg , Object foo[] ){
                    double exp = getDouble( expArg );
                    return Math.exp( exp );
                }
            } );
        set( "log" , new JSFunctionCalls1() {
                public Object call( Scope s , Object foo[] ){
                    return Double.NaN;
                }
                public Object call( Scope s , Object expArg , Object foo[] ){
                    double exp = getDouble( expArg );
                    return Math.log( exp );
                }
            } );
        
        set( "E", JSMath.E );
        lockKey( "E" );
        set( "PI", JSMath.PI );
        lockKey( "PI" );
        set( "SQRT1_2", JSMath.SQRT1_2 );
        lockKey( "SQRT1_2" );
        set( "SQRT2", JSMath.SQRT2 );
        lockKey( "SQRT2" );
        set( "LN10" , JSMath.LN10 );
        lockKey( "LN10" );
        set( "LN2" , JSMath.LN2 );
        lockKey( "LN2" );
        set( "LOG10E" , JSMath.LOG10E );
        lockKey( "LOG10E" );
        set( "LOG2E" , JSMath.LOG2E );
        lockKey( "LOG2E" );
        dontEnumExisting();
    }

    static class TrigFunc extends JSFunctionCalls1 {
        private final types type;

        private enum types {
            asin() {
                public double go( double d ) {
                    return Math.asin( d );
                }
            }, acos() {
                public double go( double d ) {
                    return Math.acos( d );
                }
            }, atan() {
                public double go( double d ) {
                    if( Double.isInfinite( d ) ) {
                        int sign = d > 0 ? 1 : -1;
                        return sign * Math.PI / 2;
                    }
                    return Math.atan( d );
                }
            }, sin() {
                public double go( double d ) {
                    return Math.sin( d );
                }
            }, cos() {
                public double go( double d ) {
                    return Math.cos( d );
                }
            }, tan() {
                public double go( double d ) {
                    return Math.tan( d );
                }
            };
            public abstract double go( double d );
        };

        public TrigFunc( String s ) {
            type = types.valueOf( s );
        }

        public Object call( Scope s , Object foo[] ){
            return Double.NaN;
        }

        public Object call( Scope s , Object xObject , Object foo[] ){
            double X = getDouble( xObject );
            return type.go( X );
        }
    };

    public static double getDouble( Object a ) {
        double d;
        if ( a == null ) {
            return 0;
        }
        else if( a instanceof Number ) {
            return ((Number)a).doubleValue();
        }
        else if( a instanceof Boolean ) {
            return ((Boolean)a).booleanValue() ? 1 : 0;
        }
        else if( ( a instanceof String || a instanceof JSString ) 
                 && a.toString().matches( POSSIBLE_NUM ) ) {
            return StringParseUtil.parseStrict(a.toString()).doubleValue();
        }
        else {
            return Double.NaN;
        }
    }

    public static double sigFig( double X ){
        return sigFig( X , 3 );
    }

    public static double sigFig( double X , double N ){
        double p = Math.pow( 10, N - Math.ceil( Math.log( Math.abs(X) ) / LN10 ) );
        return Math.round(X*p)/p;
    }

    public static final double LN10 = Math.log( 10 );
    public static final double LN2 = Math.log( 2 );
    public static final double LOG10E = Math.log10( Math.E );
    public static final double LOG2E = 1.4426950408889634;
    public static final double PI = Math.PI;
    public static final double E = Math.E;
    public static final double SQRT1_2 = Math.sqrt( .5 );
    public static final double SQRT2 = Math.sqrt( 2 );

    // matches negative, infinity, int, double, scientific notation, hex
    public static String POSSIBLE_NUM = "-?((Infinity)|(\\d+(\\.\\d+)?(e-?\\d+)?)|(0x[\\da-fA-f]+))";

}
