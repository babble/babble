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

package ed.util;

import java.io.File;

import org.testng.annotations.Test;

import ed.js.JSFunction;
import ed.js.func.JSFunctionCalls0;
import ed.util.ScriptTestInstance;

import ed.js.engine.Scope;
import ed.js.Shell;
import ed.MyAsserts;


/**
 * Dynamic test instance for testing any 10genPlatform script
 * 
 * Code stolen lock, stock and barrel from ConvertTest.  Uses exact same convention
 * and scheme for comparing output
 */
public abstract class ScriptTestInstanceBase extends MyAsserts implements ScriptTestInstance{

    File _file;

    public ScriptTestInstanceBase() {
    }
    
    public void setTestScriptFile(File f) {
    	_file = f;
    }

    public File getTestScriptFile() {
    	return _file;
    }

    /**
     *  Test method for running a script
     *  
     * @throws Exception in case of failure
     */
    @Test
    public void test() throws Exception {

        // System.out.println("ScriptTestInstanceBase : running " + _file);

        JSFunction f = convert();

        Scope scope = Scope.newGlobal().child(new File("/tmp"));
        scope.setGlobal( true );
        scope.makeThreadLocal();
        /*
         *  augment the scope
         */
        
        preTest(scope);
        
        Shell.addNiceShellStuff(scope);

        scope.put( "exit" , new JSFunctionCalls0(){
                public Object call( Scope s , Object crap[] ){
                    System.err.println("JSTestInstance : exit() called from " + _file.toString() + " Ignoring.");
                    return null;
                }
            } , true );

        ed.appserver.JSFileLibrary.addPath( f , new ed.appserver.JSFileLibrary( _file.getParentFile() , "asd" , scope ) );
        
        try {
            f.call(scope);
            validateOutput(scope);
        }
        catch (RuntimeException re) { 
            throw new Exception("For file " + _file.toString(), re);
        }
        finally {
            scope.kill();
        }
    }
}
