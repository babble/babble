// JSNumber.java

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

import ed.util.*;
import ed.js.func.*;
import ed.js.engine.*;

/** @expose
   i'm not really sure what to do with this
   right now its just holder for weird stuff
 */
public class JSNumber {

    /** Returns a given double.
     * @param A number.
     * @return <tt>x</tt>
     */
    public final static double self( double x ){
        return x;
    }

    /** Returns a given int.
     * @param A number.
     * @return <tt>x</tt>
     */
    public final static int self( int x ){
        return x;
    }

    /** A collection of numeric functions */
    public static JSObjectBase functions = new JSObjectBase();
    /** Returns a function, given its name
     * @param Function name
     * @return Function corresponding to the given name.
     */
    public static JSFunction getFunction( String name ){
        return (JSFunction)functions.get( name );
    }

    /** Function to parse a number using a given base.  */
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

    /** Function that turns a number into string, truncating any fractional part
     */
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
                    return new JSString( s );
                }

                if ( s.length() - idx <= num ){ // need more
                    int toAdd = ( num + 1 ) - ( s.length() - idx );
                    for ( int i=0; i<toAdd; i++ )
                        s += "0";
                    return new JSString( s );
                }

                if ( num == 0 )
                    return s.substring( 0 , idx  );
                return new JSString( s.substring( 0 , idx + 1 + num  ) );
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

    /** @unexpose */
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
