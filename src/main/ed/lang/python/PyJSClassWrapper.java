// PyJSClassWrapper.java

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

package ed.lang.python;

import org.python.core.*;

import ed.js.*;
import ed.js.engine.*;
import static ed.lang.python.Python.*;


public class PyJSClassWrapper extends PyJSFunctionWrapper {

    public PyJSClassWrapper( JSFunction f ){
        super(f);
    }

    public PyObject __call__(PyObject args[], String keywords[]) {

        if ( keywords != null && keywords.length > 0 )
            throw new RuntimeException( "what are keywords here..." );
        
        Object extra[] = new Object[ args == null ? 0 : args.length ];
        for ( int i=0; i<extra.length; i++ )
            extra[i] = toJS( args[i] );
        
        // TODO: not sure what to do about scope yet, but its not this probably
        JSObject n = _func.newOne();
        Scope s = _func.getScope();
	if ( s == null )
	    s = Scope.getAScope();
	s = s.child();
        s.setThis( n );
        _func.call( s , extra );
        s.clearThisNormal( null );

        return toPython( n );
    }

    public PyObject __findattr_ex__( String name ){
        if ( _func.getPrototype().containsKey( name ) )
            return toPython( _func.getPrototype().get( name ) );
        return super.__findattr_ex__( name );
    }

    public void __setattr__( String name, PyObject value ){
        JSObject proto = _func.getPrototype();
        if( value instanceof PyFunction ){
            // Have to wrap methods specially to convert implicit this ->
            // explicit self
            proto.set( name , new JSPyMethodWrapper( _func , (PyFunction)value ) );
            return;
        }
        proto.set( name, toJS( value ) );
    }
}
