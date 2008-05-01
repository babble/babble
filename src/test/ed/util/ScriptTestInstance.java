package ed.util;

import java.io.File;
import ed.js.engine.Scope;
import ed.js.JSFunction;


/**
 *  Interface for script test classes
 *
 */
public interface ScriptTestInstance {


    /**
     *   Sets the script file that is to be run
     */
    public void setTestScriptFile(File f);
    
    
    /**
     *  Called before script is run.
     */
    public void preTest(Scope s) throws Exception;

    /**
     *  Called after script is run.  
     */
    public void validateOutput(Scope s) throws Exception;
    
    /**
     *   called to get the function from the script type
     */
    
    public JSFunction convert() throws Exception;
}
