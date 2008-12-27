// CompileOptions.java

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

import ed.lang.*;

public class CompileOptions {

    public static CompileOptions forEval(){
        CompileOptions co = new CompileOptions();
        co.createNewScope( false );
        return co;
    }
    
    public CompileOptions(){
    }

    public CompileOptions copy(){
        CompileOptions options = new CompileOptions();
        options._useLocalJavaVariables = _useLocalJavaVariables;
        options._allowLoopingConstructs = _allowLoopingConstructs;
        options._createNewScope = _createNewScope;
        options._sourceLanguage = _sourceLanguage;
        return options;
    }

    /**
       doesn't work yet
     */
    public CompileOptions useLocalJavaVariables( boolean useLocalJavaVariables ){
        if ( _useLocalJavaVariables != useLocalJavaVariables ){
            _check();
            _useLocalJavaVariables = useLocalJavaVariables;
        }
        return this;
    }
    
    public boolean useLocalJavaVariables(){
        return _useLocalJavaVariables;
    }

    /**
       doesn't work yet
     */
    public CompileOptions allowLoopingConstructs( boolean allowLoopingConstructs ){
        if ( _allowLoopingConstructs != allowLoopingConstructs ){
            _check();
            _allowLoopingConstructs = allowLoopingConstructs;
        }
        return this;
    }

    public boolean allowLoopingConstructs(){
        return _allowLoopingConstructs;
    }

    public CompileOptions createNewScope( boolean createNewScope ){
        if ( _createNewScope != createNewScope ){
            _check();
            _createNewScope = createNewScope;
        }
        return this;
    }
    
    public boolean createNewScope(){
        return _createNewScope;
    }

    public CompileOptions sourceLanguage( Language sourceLanguage ){
        if ( _sourceLanguage != sourceLanguage ){
            _check();
            _sourceLanguage = sourceLanguage;
        }
        return this;
    }
    
    public Language sourceLanguage(){
        return _sourceLanguage;
    }

    public CompileOptions lock(){
        _locked = true;
        return this;
    }
    
    void _check(){
        if ( _locked )
            throw new RuntimeException( "locked" );
    }

    private boolean _locked = false;

    private boolean _useLocalJavaVariables = true;
    private boolean _allowLoopingConstructs = true;
    private boolean _createNewScope = true;
    private Language _sourceLanguage = Language.JS();
}
