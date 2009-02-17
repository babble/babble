// PyJSFunctionWrapper.java

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
