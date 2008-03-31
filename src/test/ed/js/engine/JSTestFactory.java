package ed.js.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
    @Parameters({"js-dir-name", "js-inc-regex", "js-ex-regex"})
    @Factory
    public Object[] createJSTestInstances(String jsDirName, String inclusionRegex, String exclusionRegex) {

        List<JSTestInstance> list = new ArrayList<JSTestInstance>();

        File dir = new File(jsDirName == null ? DEFAULT_DIR : jsDirName);

        Pattern inPattern = null;
        Pattern exPattern = null;
        
        if (inclusionRegex != null && inclusionRegex.length() > 0) {
            inPattern = Pattern.compile(inclusionRegex);
        }

        if (exclusionRegex != null && exclusionRegex.length() > 0) {
            exPattern = Pattern.compile(exclusionRegex);
        }

        for (File f : dir.listFiles()) {
 
            boolean include = true;
            
            if (f.toString().endsWith(JS_FILE_ENDING)) {
            
                if (inPattern != null) {
                    include = inPattern.matcher(f.toString()).matches();
                }

                if (exPattern != null) {
                    
                    if (exPattern.matcher(f.toString()).matches()) {
                        System.out.println("JSTestFactory : regexp exclusion of " + f.toString());
                        include = false;
                    }
                }
                
                if (include) {
                    list.add(new JSTestInstance(f));
                }            
            }
        }

        return list.toArray();
    }
}
