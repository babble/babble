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
public class JSNumber extends Number implements JSObject {

    public JSNumber( int i ){
	_val = i;
    }

    public JSNumber( double d ){
	_val = d;
    }

    public JSNumber( Number val ){
        _val = val;
    }

    public Number get(){
        return _val;
    }

    public byte byteValue(){
	return _val.byteValue();
    }

    public double doubleValue(){
	return _val.doubleValue();
    }
    
    public float floatValue(){
	return _val.floatValue();
    }
    
    public int intValue(){
	return _val.intValue();
    }
    
    public long longValue(){
	return _val.longValue();
    }
    
    public short shortValue(){
	return _val.shortValue();
    }

    public String toString(){
        return _val.toString();
    }

    public Object set( Object n , Object v ){
	// this seems to be a no-op
	return v;
    }

    public Object setInt( int n , Object v ){
	// this seems to be a no-op
	return v; 
    }

    public Object get( Object n ){
	return null;
    }

    public Object getInt( int n ){
	return null;
    }

    public JSFunction getFunction( String name ){
	return getFunctions().getFunction( name );
    }
    
    public Object removeField( Object n ){
	return null;
    }

    public boolean containsKey( String s ){
	return false;
    }

    public Collection<String> keySet(){
	return new LinkedList();
    }

    public Collection<String> keySet( boolean includePrototype ){
	return new LinkedList();
    }

    public JSFunction getConstructor(){
	return getCons();
    }

    public JSObject getSuper(){
	return null;
    }

    private Number _val;

    // -----------
    // STATIC STUFF BELOW
    // -----------
    
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


    /** Function to parse a number using a given base.  */
    public static class Cons extends JSFunctionCalls2{
            
        public JSObject newOne(){
            return new JSNumber( 0 );
        }
        
        public Object call( Scope scope , Object a , Object b , Object extra[] ){
            
            if ( scope.getThis() instanceof JSNumber ){
                JSNumber n = (JSNumber)(scope.getThis());
                n._val = _parse( a , b );
                return n._val;
            }
            
            return _parse( a , b );
        }
        
        Number _parse( Object a , Object b ){
            
            if ( a instanceof Number )
                return (Number)a;
            
            if ( a instanceof JSDate )
                return ((JSDate)a).getTime();
            
            if ( a != null )
                return StringParseUtil.parseNumber( a.toString() , (Number)b );
            
            if ( b != null )
                return (Number)b;
            throw new RuntimeException( "not a number [" + a + "]" );
        }
        
        protected void init(){
            _prototype.addAll( _functions );
        }
        
    }

    public static Cons getCons(){
        return (Cons)(Scope.getThreadLocalFunction( "Number" , new Cons() ) );
    }

    public static JSObjectBase getFunctions(){
        return getCons()._prototype;
    }

    private static JSObjectBase _functions = new JSObjectBase();

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
        _functions.set( "toFixed" , toFixed );

        _functions.set( "to_f" , new JSFunctionCalls0(){
                public Object call( Scope s , Object foo[] ){
                    return ((Number)s.getThis()).doubleValue();
                }
            } );

        _functions.set( "round" , new JSFunctionCalls0(){
                public Object call( Scope s , Object foo[] ){
                    return (int)( ((Number)s.getThis()).doubleValue() + .5 );
                }
            } );

        _functions.set( "times" , new JSFunctionCalls1(){
                public Object call( Scope s , Object func , Object foo[] ){
                    final int t = ((Number)s.getThis()).intValue();
                    final JSFunction f = (JSFunction)func;

                    return _loop( s , f , 0 , t );
                }
            } );

        _functions.set( "upto" , new JSFunctionCalls2(){
                public Object call( Scope s , Object num , Object func , Object foo[] ){
                    final int start = ((Number)s.getThis()).intValue();
                    final int end = ((Number)num).intValue() + 1;
                    final JSFunction f = (JSFunction)func;

                    return _loop( s , f , start , end );
                }
            } );

        _functions.set( "chr" , new JSFunctionCalls0(){
                public Object call( Scope s , Object foo[] ){
                    return (char)( ((Number)s.getThis()).intValue() );
                }
            } );

        _functions.set( "zero_q_" , new JSFunctionCalls0(){
                public Object call( Scope s , Object foo[] ){
                    return ((Number)s.getThis()).doubleValue() == 0;
                }
            } );

        _functions.set( "kilobytes" , new Conversion( 1024 ) );
        _functions.set( "megabytes" , new Conversion( 1024 * 1024 ) );

        _functions.set( "seconds" , new Conversion( 1000 ) );
        _functions.set( "minutes" , new Conversion( 1000 * 60 ) );
        _functions.set( "hours" , new Conversion( 1000 * 60 * 60 ) );
        _functions.set( "days" , new Conversion( 1000 * 60 * 60 * 24 ) );
        _functions.set( "weeks" , new Conversion( 1000 * 60 * 60 * 24 * 7 ) );
        _functions.set( "years" , new Conversion( 1000 * 60 * 60 * 24 * 365.25 ) );

    }
}
