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
                         return Double.NaN;
                     return (int)Math.floor(((Number)a).doubleValue());
                 }
             } );
        
        set( "ceil" ,
             new JSFunctionCalls1(){
                 public Object call( Scope s , Object a , Object foo[] ){
                     if ( a == null )
                         return 0;
                     if ( ! ( a instanceof Number ) )
                         return Double.NaN;
                     return (int)Math.ceil(((Number)a).doubleValue());
                 }
             } );
        
        set( "abs" ,
             new JSFunctionCalls1(){
                 public Object call( Scope s , Object a , Object foo[] ){
                     if ( a == null ) 
                         return 0;
                     if ( ! ( a instanceof Number ) )
                         a = StringParseUtil.parseNumber(a.toString(), Double.NaN);

                     if ( a instanceof Integer )
                         return Math.abs(((Integer)a).intValue());
                     return Math.abs(((Number)a).doubleValue());
                     
                 }

             } );
    }
    

    
}
