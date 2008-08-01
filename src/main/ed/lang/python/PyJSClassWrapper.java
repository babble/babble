// PyJSClassWrapper.java

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
        s.setThis( n );
        _func.call( s , extra );
        s.clearThisNormal( null );

        return toPython( n );
    }

    public PyObject __findattr__( String name ){
        if ( _func.getPrototype().containsKey( name ) )
            return toPython( _func.getPrototype().get( name ) );
        return super.__findattr__( name );
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
