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

    /**
       doesn't work yet
     */
    public CompileOptions useLocalVariables( boolean useLocalVariables ){
        this.useLocalVariables = useLocalVariables;
        return this;
    }

    /**
       doesn't work yet
     */
    public CompileOptions allowLoopingConstructs( boolean allowLoopingConstructs ){
        this.allowLoopingConstructs = allowLoopingConstructs;
        return this;
    }

    public CompileOptions createNewScope( boolean createNewScope ){
        this.createNewScope = createNewScope;
        return this;
    }

    public CompileOptions sourceLanguage( Language sourceLanguage ){
        this.sourceLanguage = sourceLanguage;
        return this;
    }

    public boolean useLocalVariables = true;
    public boolean allowLoopingConstructs = true;
    public boolean createNewScope = true;
    public Language sourceLanguage = Language.JS;
}
