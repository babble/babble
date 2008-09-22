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
                 public Object call( Scope s , Object a , Object b , Object foo[] ){

                     if ( a != null && ! ( a instanceof Number ) )
                         return Double.NaN;

                     if ( b != null && ! ( b instanceof Number ) )
                         return Double.NaN;

                     if ( a == null && b == null )
                         return 0;

                     if ( a == null )
                         return b;

                     if ( b == null )
                         return a;

                     return ((Number)a).doubleValue() > ((Number)b).doubleValue() ? a : b;
                 }
             } );

        set( "min" ,
             new JSFunctionCalls2(){
                 public Object call( Scope s , Object a , Object b , Object foo[] ){

                     if ( a != null && ! ( a instanceof Number ) )
                         return Double.NaN;

                     if ( b != null && ! ( b instanceof Number ) )
                         return Double.NaN;

                     if ( a == null && b == null )
                         return 0;

                     if ( a == null )
                         return b;

                     if ( b == null )
                         return a;

                     return ((Number)a).doubleValue() < ((Number)b).doubleValue() ? a : b;
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
                 public Object call( Scope s , Object a , Object foo[] ){
                     if ( a == null )
                         return 0;
                     if ( ! ( a instanceof Number ) )
                         try {
                             a = StringParseUtil.parseStrict(a.toString());
                         }
                         catch (Exception e) {
                             return Double.NaN;
                         }
                     return (int)Math.floor(((Number)a).doubleValue());
                 }
             } );

        set( "ceil" ,
             new JSFunctionCalls1(){
                 public Object call( Scope s , Object a , Object foo[] ){
                     if ( a == null )
                         return 0;
                     if ( ! ( a instanceof Number ) )
                         try {
                             a = StringParseUtil.parseStrict(a.toString());
                         }
                         catch (Exception e) {
                             return Double.NaN;
                         }
                     return (int)Math.ceil(((Number)a).doubleValue());
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
                 public Object call( Scope s , Object a , Object foo[] ){
                     if ( a == null )
                         return 0;
                     if ( ! ( a instanceof Number ) )
                         try {
                             a = StringParseUtil.parseStrict(a.toString());
                         }
                         catch (Exception e) {
                             return Double.NaN;
                         }

                     if ( a instanceof Integer )
                         return Math.abs(((Integer)a).intValue());
                     return Math.abs(((Number)a).doubleValue());

                 }

             } );


        set( "sqrt" ,
             new JSFunctionCalls1(){
                 public Object call( Scope s , Object a , Object foo[] ){
                     if ( a == null )
                         return 0;
                     if ( ! ( a instanceof Number ) )
                         try {
                             a = StringParseUtil.parseStrict(a.toString());
                         }
                         catch (Exception e) {
                             return Double.NaN;
                         }

                     return Math.sqrt( ((Number)a).doubleValue() );
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

        set( "sin" , new JSFunctionCalls2(){
            public Object call( Scope s , Object xObject , Object nObject , Object foo[] ){
                double X = ((Number)xObject).doubleValue();
                return Math.sin(X);
            }
        } );

        set( "cos" , new JSFunctionCalls2(){
            public Object call( Scope s , Object xObject , Object nObject , Object foo[] ){
                double X = ((Number)xObject).doubleValue();
                return Math.cos(X);
            }
        } );

        set( "pow" , new JSFunctionCalls2(){
            public Object call( Scope s , Object baseArg , Object expArg , Object foo[] ){
                double base = ((Number)baseArg).doubleValue();
                double exp = ((Number)expArg).doubleValue();
                return Math.pow(base, exp);
            }
        } );
        
        set( "PI", JSMath.PI );
        lockKey( "PI" );
        set( "SQRT1_2", JSMath.SQRT1_2 );
        lockKey( "SQRT1_2" );

    }

    public static double sigFig( double X ){
        return sigFig( X , 3 );
    }

    public static double sigFig( double X , double N ){
        double p = Math.pow( 10, N - Math.ceil( Math.log( Math.abs(X) ) / LN10 ) );
        return Math.round(X*p)/p;
    }

    public static final double LN10 = Math.log(10);
    public final static double PI = Math.PI;
    public final static double SQRT1_2 = Math.sqrt( .5 );

}
