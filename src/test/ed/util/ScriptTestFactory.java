package ed.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.testng.annotations.Factory;
import org.testng.annotations.Parameters;
import org.testng.annotations.Optional;


/**
 * Factory to create tests for each JS test we have.  
 * Will be invoked by TestNG framework and make a test for each .js file
 */
public class ScriptTestFactory {
    
    String _dir = null;
    
    /**
     *  Creates an array of ScriptTestInstance objects, each representing
     *  one script file in whatever directory we're targeted at
     */
    @Parameters({ "test-class", "dir-name", "file-ending", "inc-regex", "ex-regex"})
    @Factory
    public Object[] createTestInstances(String testClassName, String dirName, String fileEnding, 
                                        @Optional("") String inclusionRegex,
                                        @Optional("") String exclusionRegex) throws Exception {


        if (testClassName == null || testClassName.length() == 0) { 
            throw new Exception("config error : test class name unspecified");
        }
        
        
        /*
         *   ensure that the specified class exists and is useful
         */
        try { 
            ScriptTestInstance i = (ScriptTestInstance) Class.forName(testClassName).newInstance();
        }
        catch(ClassCastException cce) { 
            throw new Exception("config error : specified test class not a ScriptTestInstance?", cce);
        }
        
        if (dirName == null || dirName.length() == 0) { 
            throw new Exception("config error : test directory unspecified");
        }

        if (fileEnding == null || fileEnding.length() == 0) { 
            throw new Exception("config error : file ending unspecified");
        }

        List<ScriptTestInstance> list = new ArrayList<ScriptTestInstance>();
        
        File dir = new File(dirName);
        
        if (!dir.exists()) {
            throw new Exception("config error : test directory doesn't exist");
        }

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
            
            if (f.toString().endsWith(fileEnding)) {
            
                if (inPattern != null) {
                    include = inPattern.matcher(f.toString()).matches();
                    
                    ScriptTestInstance testInstance = (ScriptTestInstance) Class.forName(testClassName).newInstance();
                    testInstance.setTestScriptFile(f);
                    list.add(testInstance);
                    continue;
                }

                if (exPattern != null) {

                    if (exPattern.matcher(f.toString()).matches()) {
                        System.out.println("JSTestFactory : regexp exclusion of " + f.toString());
                        include = false;
                    }
                }
                
                if (include) {
                    ScriptTestInstance testInstance = (ScriptTestInstance) Class.forName(testClassName).newInstance();
                    testInstance.setTestScriptFile(f);
                    list.add(testInstance);
                }            
            }
        }

        return list.toArray();
    }
}
