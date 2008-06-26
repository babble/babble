package ed.js.engine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import ed.MyAsserts;
import ed.io.StreamUtil;
import ed.js.JSFunction;
import ed.js.func.JSFunctionCalls1;
import ed.util.ScriptTestInstanceBase;

/**
 * Dynamic test instance for testing the Javascript
 * 
 */
public class JSTestInstance extends ScriptTestInstanceBase{

    public JSTestInstance() {
    }

    ByteArrayOutputStream bout = new ByteArrayOutputStream();

    public JSFunction convert() throws Exception{
        Convert c = new Convert(getTestScriptFile());
        
        return c.get();
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
