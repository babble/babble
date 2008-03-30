package ed.js.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Factory;
import org.testng.annotations.Parameters;

/**
 * Factory to create tests for each JS test we have.  
 * Will be invoked by TestNG framework and make a test for each .js file
 */
public class JSTestFactory {
    
    static final String JS_FILE_ENDING = ".js";
    static final String DEFAULT_DIR = "src/test/ed/js/engine/";
    String _dir = DEFAULT_DIR;
    
    /**
     *  Creates an array of JSTestIntance objects, each representing
     *  one js file in whatever directory we're targeted at
     */
    @Parameters({"js-dir-name"})
    @Factory
    public Object[] createJSTestInstances(String jsDirName) {

        List<JSTestInstance> list = new ArrayList<JSTestInstance>();

        File dir = new File(jsDirName == null ? DEFAULT_DIR : jsDirName);

        _addCases(list, dir);

        return list.toArray();
    }
    
    private void _addCases(List<JSTestInstance> list, File dir) {
        
        for (File f : dir.listFiles()) {
            
            if (f.toString().endsWith(JS_FILE_ENDING)){
                list.add(new JSTestInstance(f));
            }
        }
    }
 }
