// JSNumber.java

package ed.js;

import java.util.*;

import ed.util.*;
import ed.js.func.*;
import ed.js.engine.*;

/**
   i'm not really sure what to do with this
   right now its just holder for weird stuff
 */
public class JSNumber {


    public static JSObjectBase functions = new JSObjectBase();
    public static JSFunction getFunction( String name ){
        return (JSFunction)functions.get( name );
    }

    public static final JSFunction CONS = new JSFunctionCalls2(){
            public Object call( Scope scope , Object a , Object b , Object extra[] ){
                
                if ( a instanceof Number )
                    return a;
                
                if ( a != null )
                    return StringParseUtil.parseNumber( a.toString() , (Number)b );
                
                if ( b != null )
                    return b;
                throw new RuntimeException( "not a number [" + a + "]" );
            }

            
        };
    
    
    public static JSFunction toFixed = new JSFunctionCalls1(){
            public Object call( Scope sc , Object pointsInt , Object fo00000o[] ){
                Number n = (Number)(sc.getThis());
                int num = 0;
                if ( pointsInt != null && pointsInt instanceof Number )
                    num = ((Number)pointsInt).intValue();
                
                double mult = Math.pow( 10 , num );
                
                long foo = Math.round( n.doubleValue() * mult );
                double d = foo;
                d = d / mult ;
                
                String s = String.valueOf( d );
                int idx = s.indexOf( "." );
                
                if ( idx < 0 ){
                    if( num > 0 ){
                        s += ".";
                        while( num > 0 ){
                            s += "0";
                            num--;
                        }
                    }
                    return s;
                }

                if ( s.length() - idx <= num ){ // need more
                    int toAdd = ( num + 1 ) - ( s.length() - idx );
                    for ( int i=0; i<toAdd; i++ )
                        s += "0";
                    return s;
                }
                
                if ( num == 0 )
                    return s.substring( 0 , idx  );
                return s.substring( 0 , idx + 1 + num  );
            }
        };
    
    public static class Conversion extends JSFunctionCalls0{
        
        Conversion( double conversion ){
            _conversion = conversion;
        }
        
        public Object call( Scope s , Object foo[] ){
            Number n = (Number)s.getThis();
            return n.doubleValue() * _conversion;
        }
        
        final double _conversion;
    }

    static Object _loop( final Scope s , final JSFunction f , final int start , final int end ){
        Object blah = s.getParent().getThis();
        s.setThis( blah );
        
        Boolean old = f.setUsePassedInScopeTL( true );
        
        for ( int i=start; i<end; i++ )
            f.call( s , i );
        
        f.setUsePassedInScopeTL( old );
        
        s.clearThisNormal( null );
        
        return null;        
    }
    
    static {
        functions.set( "toFixed" , toFixed );
        
        functions.set( "to_f" , new JSFunctionCalls0(){
                public Object call( Scope s , Object foo[] ){
                    return ((Number)s.getThis()).doubleValue();
                }
            } );
        
        functions.set( "round" , new JSFunctionCalls0(){
                public Object call( Scope s , Object foo[] ){
                    return (int)( ((Number)s.getThis()).doubleValue() + .5 );
                }
            } );
        
        functions.set( "times" , new JSFunctionCalls1(){
                public Object call( Scope s , Object func , Object foo[] ){
                    final int t = ((Number)s.getThis()).intValue();
                    final JSFunction f = (JSFunction)func;
                    
                    return _loop( s , f , 0 , t );
                }
            } );

        functions.set( "upto" , new JSFunctionCalls2(){
                public Object call( Scope s , Object num , Object func , Object foo[] ){
                    final int start = ((Number)s.getThis()).intValue();
                    final int end = ((Number)num).intValue() + 1;
                    final JSFunction f = (JSFunction)func;
                    
                    return _loop( s , f , start , end );
                }
            } );

        functions.set( "chr" , new JSFunctionCalls0(){
                public Object call( Scope s , Object foo[] ){
                    return (char)( ((Number)s.getThis()).intValue() );
                }
            } );

        functions.set( "zero_q_" , new JSFunctionCalls0(){
                public Object call( Scope s , Object foo[] ){
                    return ((Number)s.getThis()).doubleValue() == 0;
                }
            } );

        functions.set( "kilobytes" , new Conversion( 1024 ) );
        functions.set( "megabytes" , new Conversion( 1024 * 1024 ) );

        functions.set( "seconds" , new Conversion( 1000 ) );
        functions.set( "minutes" , new Conversion( 1000 * 60 ) );
        functions.set( "hours" , new Conversion( 1000 * 60 * 60 ) );
        functions.set( "days" , new Conversion( 1000 * 60 * 60 * 24 ) );
        functions.set( "weeks" , new Conversion( 1000 * 60 * 60 * 24 * 7 ) );
        functions.set( "years" , new Conversion( 1000 * 60 * 60 * 24 * 365.25 ) );

    }
}

