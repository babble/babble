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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import ed.MyAsserts;
import ed.io.StreamUtil;
import ed.js.JSFunction;
import ed.js.func.JSFunctionCalls1;
import ed.util.ScriptTestInstanceBase;
import ed.appserver.JSFileLibrary;
import ed.lang.python.PythonJxpSource;

/**
 * Dynamic test instance for testing the Javascript
 * 
 */
public class JSTestInstance extends ScriptTestInstanceBase{

    public JSTestInstance() {
    }

    ByteArrayOutputStream bout = new ByteArrayOutputStream();

    public JSFunction convert() throws Exception{
        // FIXME: This is a bit of a hack -- shouldn't we do this elsewhere?
        File f = getTestScriptFile();
        if(f.toString().endsWith(".js")){
            Convert c = new Convert(getTestScriptFile());
            return c.get();
        }
        else if(f.toString().endsWith(".py")){
            PythonJxpSource py = new PythonJxpSource( getTestScriptFile() , new JSFileLibrary( new File( "." ) , "local" , (Scope)null ) );
            return py.getFunction();
        }
        else {
            throw new RuntimeException("couldn't run " + f);
        }
    }
    
    public void preTest(Scope scope) throws Exception {
    	
    	File jsFile = getTestScriptFile();
    
        final PrintStream out = new PrintStream(bout);    

        if (jsFile.toString().contains("/engine/")) {
            JSFunction myout = new JSFunctionCalls1() {
                public Object call(Scope scope ,Object o , Object extra[]){
                    out.println(ed.js.JSInternalFunctions.JS_toString(o));
                    return null;
                }
            };

            scope.put("print" , myout , true);
            scope.put("SYSOUT" , myout , true);
        }
        
    }
    
    public void validateOutput(Scope scope) throws Exception {
    	
        String outString = _clean( bout.toString());
    
    	File jsFile = getTestScriptFile();

        if (_isEngineFile()) {
            File correct = new File(jsFile.toString().replaceAll(".js$", ".out"));
        
            if (!correct.exists()) {
                throw new Exception("Error - no correct file for " + jsFile.toString());
            }
            
            String correctOut = _clean(StreamUtil.readFully(correct));
        
            if (!MyAsserts.isClose(correctOut, outString)) {
                System.out.println();
                System.out.println( correctOut.replaceAll("[\r\n ]+" , " "));
                System.out.println( outString.replaceAll("[\r\n ]+" , " "));
                throw new Exception(" for test " + jsFile.toString() + " : [" + correctOut + "] != [" + outString + "]");
            }
        }
    }

    private boolean _isEngineFile() { 
        return getTestScriptFile().toString().contains("/engine/");
    }
    
    private String _clean(String s) {
        return ConvertTest._clean( s );
    }
    
    public static void main(String[] args) throws Exception {
    	
    	JSTestInstance jsti = new JSTestInstance();
    	
    	jsti.setTestScriptFile(new File(args[0]));
    	
    	jsti.test();
    }
}
