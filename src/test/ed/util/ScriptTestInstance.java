package ed.util;

import java.io.File;
import ed.js.engine.Scope;


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
}
