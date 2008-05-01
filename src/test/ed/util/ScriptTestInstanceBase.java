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

        System.out.println("ScriptTestInstanceBase : running " + _file);

        JSFunction f = convert();

        Scope scope = Scope.GLOBAL.child(new File("/tmp"));
    
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

        try {
            f.call(scope);
        }
        catch (RuntimeException re) { 
            throw new Exception("For file " + _file.toString(), re);
        }
            
        validateOutput(scope);
    }
}