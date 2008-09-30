// Language.java

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

package ed.lang;

import javax.script.*;

import ed.js.*;
import ed.js.engine.*;

public abstract class Language {
    
    protected Language( String name ){
        _name = name;
    }
    
    public boolean isScriptable(){
        return false;
    }

    public ObjectConvertor getObjectConvertor(){
        throw new UnsupportedOperationException();
    }

    public ScriptEngine getScriptEngine(){
        throw new UnsupportedOperationException();
    }

    public JSFunction compileLambda( String source ){
        throw new UnsupportedOperationException();
    }

    public Object eval( Scope scope , String code , boolean[] hasReturn ){
        JSFunction func = compileLambda( code );
        Object ret = func.call( scope );
        if ( hasReturn != null && hasReturn.length > 0 )
            hasReturn[0] = true;
        return ret;
    }

    public String toString(){
        return _name;
    }

    final String _name;
    
    public static final Language JS = new ed.js.JS();
    public static final Language RUBY = new ed.lang.ruby.RubyLanguage();
    public static final Language PYTHON = new ed.lang.python.Python();

    // instantiate lazily so PHP can be optional
    public static  Language PHP;

    public static Language find( String file ){
        return find( file , false );
    }

    public static Language find( String file , boolean errorOnNoMatch ){
        
        final int idx = file.lastIndexOf( "." );

        final String extension;
        if ( idx >= 0 )
            extension = file.substring( idx + 1 );
        else
            extension = file;
        
        if ( extension.equals( "js" )
             || extension.equals( "jxp" ) )
            return JS;

        if ( extension.equals( "rb" )
             || extension.equals( "erb" )
             || extension.equals( "rhtml" )
	     || extension.equals( "ruby" ) ) // only so "--ruby" will work
            return RUBY;

        if ( extension.equals( "php" ) ) {

            /*
             *   lazy instantiation - if this method isn't static at some point, protect this 
             */
            if (PHP == null) {
                PHP = new ed.lang.php.PHP();
            }
            return PHP;
        }
        
        if ( extension.equals( "py" ) || extension.equals( "python" ) )
            return PYTHON;
        
        if ( errorOnNoMatch )
            throw new RuntimeException( "no language for [" + extension + "]" );

        return JS;
    }
        
}
