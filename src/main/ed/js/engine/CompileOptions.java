// CompileOptions.java

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
