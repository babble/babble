// JSMath.java

package ed.js;
import ed.util.StringParseUtil;

import ed.js.func.*;
import ed.js.engine.*;

public class JSMath extends JSObjectBase {

    private static JSMath _instance = new JSMath();
    public static JSMath getInstance(){
        return _instance;
    }
    
    private JSMath(){
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
        
    }
    
    public static double sigFig( double X ){
        return sigFig( X , 3 );
    }
    
    public static double sigFig( double X , double N ){
        double p = Math.pow( 10, N - Math.ceil( Math.log( Math.abs(X) ) / LN10 ) );
        return Math.round(X*p)/p;
    }
    
    public static final double LN10 = Math.log(10);

}
