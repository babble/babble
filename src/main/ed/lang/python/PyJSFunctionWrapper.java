// PyJSFunctionWrapper.java

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


public class PyJSFunctionWrapper extends PyJSObjectWrapper {

    public PyJSFunctionWrapper( JSFunction func ){
        this( func , null );
    }

    public PyJSFunctionWrapper( JSFunction func , Object useThis ){
        super( func );
        
        if ( func == null )
            throw new IllegalArgumentException( "can't create wrapper with null function" );
        
        _func = func;
        _this = useThis;
    }
    
    public PyObject __call__(PyObject args[], String keywords[]) {

        if ( keywords != null && keywords.length > 0 )
            throw new RuntimeException( "what are keywords here..." );
        
        Object extra[] = new Object[ args == null ? 0 : args.length ];
        for ( int i=0; i<extra.length; i++ )
            extra[i] = toJS( args[i] );
        
        Scope s = _func.getScope();
        if ( s == null )
            s = Scope.getAScope();
        s = s.child();
        
        s.setThis( _this );
        return toPython( _func.call( s , extra ) );
    }

    final JSFunction _func;
    final Object _this;
}
