// JSBoolean.java

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
                        if( o instanceof Boolean ) 
                            return o;
                        if( !(o instanceof JSBoolean ) )
                            throw new JSException( "error" );

                        return ((JSBoolean)o).value;
                    }
                } );
            _prototype.set( "toString", new JSFunctionCalls0() {
                    public Object call( Scope s , Object foo[] ){
                        return new JSString( s.getThis().toString() );
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
        this( JSInternalFunctions.VOID );
    }

    public JSBoolean( Object o ) {
        super( _getCons() );
        init( o );
    }

    public String toString() {
        return value + "";
    }

    public boolean value = false;

    private static JSFunction _getCons(){
        return Scope.getThreadLocalFunction( "Boolean" , _cons );
    }

    public static boolean booleanValue( Object o ) {
        if( o == null ||
            o instanceof JSInternalFunctions.Void )
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
            return !Double.isNaN( d ) && d != 0;
        }

        if ( o instanceof JSObject ) {
            return true;
        }

        return true;
    }
}
