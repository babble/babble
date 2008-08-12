// JSCompiledScript.java

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

package ed.js.engine;

import java.util.*;

import ed.ext.org.mozilla.javascript.*;

import ed.lang.*;
import ed.util.*;
import ed.js.*;
import ed.js.func.*;

public abstract class JSCompiledScript extends JSFunctionCalls0 {
 
    protected abstract Object _call( Scope scope , Object extra[] ) throws Exception;
    
    public Object call( Scope scope , Object extra[] ){
        try {
            return _call( scope, extra );
        }
        catch ( RuntimeException re ){
            if ( Convert.DJS ) re.printStackTrace();
            _convert.fixStack( re );
            throw re;
        }
        catch ( Exception e ){
            e.printStackTrace();
            if ( Convert.DJS ) e.printStackTrace();
            _convert.fixStack( e );
            throw new RuntimeException( "weird error : " + e.getClass().getName() , e );
        }
    }
    
    protected void _throw( Object foo ){
        
        if ( foo instanceof JSException )
            throw ( JSException)foo;
        
        if ( foo instanceof Throwable )
            throw new JSException( foo.toString() , (Throwable)foo );
        
        throw new JSException( foo );
    }

    public Language getFileLanguage(){
        if ( _convert == null )
            return Language.JS;
        return _convert._sourceLanguage;
    }
    
    public JSString _string( int id ){
	if ( _jsstrings == null )
	    _jsstrings = new JSString[ _strings.length ];
	
	if ( _jsstrings[id] == null )
	    _jsstrings[id] = new JSString( _strings[id] );

	return _jsstrings[id];
    }

    public JSRegex _regex( int id ){
        Pair<String,String> p = _regex.get( id );
        return new JSRegex( p.first , p.second );
    }

    Convert _convert;
    protected List<Pair<String,String>> _regex;
    protected String _strings[];
    protected JSString _jsstrings[];
}
