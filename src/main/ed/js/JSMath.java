// JSMath.java

/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
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
                     return Double.NEGATIVE_INFINITY;
                 }
                 public Object call( Scope s , Object a , Object b , Object foo[] ){
                     double d1 = JSNumber.getDouble( a );
                     double d2 = JSNumber.getDouble( b );
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
                     double d1 = JSNumber.getDouble( a );
                     double d2 = JSNumber.getDouble( b );
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
                     double d = JSNumber.getDouble( a );
                     if( Double.isNaN( d ) || 
                         Double.isInfinite( d ) || 
                         d == 0 ) {
                         return d;
                     }
                     return (long)Math.floor( d );
                 }
             } );

        set( "ceil" ,
             new JSFunctionCalls1(){
                 public Object call( Scope s , Object foo[] ) {
                     return Double.NaN;
                 }
                 public Object call( Scope s , Object a , Object foo[] ){
                     double d = JSNumber.getDouble( a );
                     if( Double.isNaN( d ) || 
                         Double.isInfinite( d ) ||
                         d == 0 ) {
                         return d;
                     }
                     return Math.ceil( d );
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
                     // Java returns 0 for Math.round(NaN) and Math.round( -0 ), 
                     // MIN_VALUE for Math.round( -inf ), and MAX_VALUE for 
                     // Math.round( inf )
                     // JavaScript returns:
                     if( a.equals( Double.NaN ) )
                         return Double.NaN;
                     if( a.equals( Double.POSITIVE_INFINITY ) ) 
                         return Double.POSITIVE_INFINITY;
                     if( a.equals( Double.NEGATIVE_INFINITY ) ) 
                         return Double.NEGATIVE_INFINITY;
                     if( a.toString().equals( "-0" ) || 
                         a.toString().equals( "-0.0" ) ||
                         ( ((Number)a).doubleValue() >= -.5 &&
                           ((Number)a).doubleValue() < 0 ) )
                         return -0.0;
                     return Math.round(((Number)a).doubleValue());
                 }
             } );

        set( "abs" ,
             new JSFunctionCalls1(){
                 public Object call( Scope s , Object foo[] ){
                     return Double.NaN;
                 }
                 public Object call( Scope s , Object a , Object foo[] ){
                     return Math.abs( JSNumber.getDouble( a ) );
                 }
             } );


        set( "sqrt" ,
             new JSFunctionCalls1(){
                 public Object call( Scope s , Object foo[] ){
                     return Double.NaN;
                 }
                 public Object call( Scope s , Object a , Object foo[] ){
                     return Math.sqrt( JSNumber.getDouble( a ) );
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
                    double d1 = JSNumber.getDouble( a );
                    double d2 = JSNumber.getDouble( b );
                    return Math.atan2( d1 , d2 );
                }
            } );

        set( "pow" , new JSFunctionCalls2(){
                public Object call( Scope s , Object foo[] ){
                    return Double.NaN;
                }
                public Object call( Scope s , Object baseArg , Object expArg , Object foo[] ){
                    double base = JSNumber.getDouble( baseArg );
                    double exp = JSNumber.getDouble( expArg );
                    double r = Math.pow(base, exp);
                    if( r != 0 &&
                        !Double.isInfinite( r ) &&
                        Math.floor( r ) == r )
                        return (long)r;
                    return r;
                }
            } );
        set( "exp" , new JSFunctionCalls1(){
                public Object call( Scope s , Object foo[] ){
                    return Double.NaN;
                }
                public Object call( Scope s , Object expArg , Object foo[] ){
                    double exp = JSNumber.getDouble( expArg );
                    return Math.exp( exp );
                }
            } );
        set( "log" , new JSFunctionCalls1() {
                public Object call( Scope s , Object foo[] ){
                    return Double.NaN;
                }
                public Object call( Scope s , Object expArg , Object foo[] ){
                    double exp = JSNumber.getDouble( expArg );
                    return Math.log( exp );
                }
            } );
        
        set( "E", JSMath.E );
        set( "PI", JSMath.PI );
        set( "SQRT1_2", JSMath.SQRT1_2 );
        set( "SQRT2", JSMath.SQRT2 );
        set( "LN10" , JSMath.LN10 );
        set( "LN2" , JSMath.LN2 );
        set( "LOG10E" , JSMath.LOG10E );
        set( "LOG2E" , JSMath.LOG2E );
        setProperties( "E", JSObjectBase.LOCK );
        setProperties( "PI", JSObjectBase.LOCK );
        setProperties( "SQRT1_2", JSObjectBase.LOCK );
        setProperties( "SQRT2", JSObjectBase.LOCK );
        setProperties( "LN10", JSObjectBase.LOCK );
        setProperties( "LN2", JSObjectBase.LOCK );
        setProperties( "LOG10E", JSObjectBase.LOCK );
        setProperties( "LOG2E", JSObjectBase.LOCK );
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
            double X = JSNumber.getDouble( xObject );
            return type.go( X );
        }
    };

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

}
