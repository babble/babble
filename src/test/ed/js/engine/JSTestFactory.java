package ed.js.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Factory;;

/**
 * Factory to create tests for each JS test we have.  
 * Will be invoked by TestNG framework and make a test for each .js file
 */
public class JSTestFactory {
    
    static final String JS_FILE_ENDING = ".js";
    static final String DEFAULT_DIR = "src/test/ed/js/engine/";
    String _dir = DEFAULT_DIR;
    
    /**
     *  Default CTOR - will use src/test/ed/js/engine for testcases
     */
    public JSTestFactory() {
    }
        
    /**
     *  CTOR that allows the factory to be aimed at any directory
     *  
     *  @param jsDirectory directory to find javascript test files and their 
     *                     corresponding output files for comparison
     */
    public JSTestFactory(String jsDirectory) {
        _dir = jsDirectory;
    }
    
    /**
     *  Creates an array of JSTestIntance objects, each representing
     *  one js file in whatever directory we're targeted at
     */
    @Factory
    public Object[] createJSTestInstances() {
 
        File dir = new File(_dir);
        
        List<JSTestInstance> list = new ArrayList<JSTestInstance>();
        
        for (File f : dir.listFiles()) {
            
            if (f.toString().endsWith(JS_FILE_ENDING)){
                list.add(new JSTestInstance(f));
            }
        }
        
        return list.toArray();
    }
 }
