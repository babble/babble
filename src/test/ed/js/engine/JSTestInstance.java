package ed.js.engine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.testng.annotations.Test;

import ed.MyAsserts;
import ed.io.StreamUtil;
import ed.js.JSFunction;
import ed.js.func.JSFunctionCalls1;

/**
 * Dynamic test instance for testing the javascript in a way that pleases Eliot :)
 * 
 * Code stolen lock, stock and barrel from ConvertTest.  Uses exact same convention
 * and scheme for comparing output
 */
public class JSTestInstance {

    final File _jsFile;
    
    public JSTestInstance(File file) {
        _jsFile = file;
    }

    /**
     *  Testmethod for running a js test.  Same code as 
     *  ConvertTest
     *  
     * @throws Exception in case of failure
     */
    @Test
    public void test() throws Exception {
        
        Convert c = new Convert(_jsFile);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final PrintStream out = new PrintStream(bout);
    
        JSFunction f = c.get();
        Scope scope = Scope.GLOBAL.child();
    
        if (_jsFile.toString().contains("/engine/")) {
            JSFunction myout = new JSFunctionCalls1() {
                public Object call(Scope scope ,Object o , Object extra[]){
                    out.println(o);
                    return null;
                }
            };

            scope.put("print" , myout , true);
            scope.put("SYSOUT" , myout , true);
        }

        try {
            f.call(scope);
        }
        catch (RuntimeException re) { 
            throw new Exception("For file " + _jsFile.toString(), re);
        }
        
        String outString = _clean( bout.toString());
    
        if (_isEngineFile()) {
            File correct = new File(_jsFile.toString().replaceAll(".js$", ".out"));
        
            if (!correct.exists()) {
                throw new Exception("Error - no correct file for " + _jsFile.toString());
            }
            
            String correctOut = _clean(StreamUtil.readFully(correct));
        
            if (!MyAsserts.isClose(correctOut, outString)) {
                System.out.println();
                System.out.println( correctOut.replaceAll("[\r\n ]+" , " "));
                System.out.println( outString.replaceAll("[\r\n ]+" , " "));
                throw new Exception(" for test " + _jsFile.toString() + " : [" + correctOut + "] != [" + outString + "]");
            }
        }
    }

    private boolean _isEngineFile() { 
        return _jsFile.toString().contains("/engine/");
    }
    
    private String _clean(String s) {
        s = s.replaceAll("tempFunc_\\d+_" , "tempFunc_");
        return s;
    }
}