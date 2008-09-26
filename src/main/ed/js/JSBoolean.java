// JSBoolean.java

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

public class JSBoolean extends JSObjectBase {

    public static JSFunction _cons = new Cons();

    public static class Cons extends JSFunctionCalls1{

        public JSObject newOne(){
            return new JSBoolean();
        }

        public Object call( Scope scope , Object a , Object[] extra ){
            Object foo = scope.getThis();

            JSBoolean e;
            if( foo instanceof JSBoolean ) {
                e = (JSBoolean)foo;
                e.init( a );
            }
            else {
                e = new JSBoolean( a );
            }
            return e.value;
        }

        protected void init(){
            _prototype.set( "valueOf" , new JSFunctionCalls0() {
                    public Object call( Scope s , Object foo[] ){
                        Object o = s.getThis();
                        if( !(o instanceof JSBoolean ) )
                            throw new JSException( "error" );

                        return ((JSBoolean)o).value;
                    }
                } );
            _prototype.set( "toString" , new JSFunctionCalls0() {
                    public Object call( Scope s , Object foo[] ) {
                        Object o = s.getThis();
                        if( !(o instanceof JSBoolean ) )
                            throw new JSException( "error" );

                        return ((JSBoolean)o).value + "";
                    }
                } );

            _prototype.dontEnumExisting();
            dontEnum( "prototype" );
        }
    }

    public void init( Object o ) {
        value = JSBoolean.booleanValue( o );
    }

    public JSBoolean() {
        this( null );
    }

    public JSBoolean( Object o ) {
        super( _getCons() );
        init( o );
    }

    public boolean value = false;

    private static JSFunction _getCons(){
        return Scope.getThreadLocalFunction( "Boolean" , _cons );
    }

    public static boolean booleanValue( Object o ) {
        if( o == null )
            return false;

        if( o instanceof JSBoolean ) {
            return ((JSBoolean)o).value;
        }
        else if ( o instanceof Boolean ) {
            return ((Boolean)o).booleanValue();
        }

        if ( o instanceof JSString ||
             o instanceof String ) {
            return o.toString().length() > 0;
        }

        if ( o instanceof Number ) {
            double d = ((Number)o).doubleValue();
            return d == 1 || d == -1 || Double.isInfinite( d );
        }

        if ( o instanceof JSObject ) {
            return true;
        }

        return false;
    }
}
