// RubyScriptTestInstance.java

package ed.lang.ruby;

import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import ed.js.JSFunction;
import ed.js.func.JSFunctionCalls1;
import ed.js.engine.Scope;
import ed.util.ScriptTestInstanceBase;
import ed.io.StreamUtil;

/**
 * Ruby script testing class for the script testing framework
 *
 */
public class RubyScriptTestInstance extends ScriptTestInstanceBase {

    private ByteArrayOutputStream bout = new ByteArrayOutputStream();

    public RubyScriptTestInstance() {
    }

    public JSFunction convert() throws Exception {
    	RubyConvert c = new RubyConvert(getTestScriptFile());
    	
    	return c.get();
    }

   public void preTest(Scope scope) throws Exception {
    	
        final PrintStream out = new PrintStream( bout );

        JSFunction myout = new JSFunctionCalls1(){
                public Object call( Scope scope ,Object o , Object extra[] ){
                    out.println( o );
                    return null;
                }
            };

        scope.put( "print" , myout , true );
        scope.put( "puts" , myout , true );
        scope.put( "__puts__" , myout , true );
        scope.put( "SYSOUT" , myout , true );
    }

    public void validateOutput(Scope scope) throws Exception {

        String outString = _clean( bout.toString() );

        File correct = new File( getTestScriptFile().toString().replaceAll( ".rb$" , ".out" ) );
        if ( ! correct.exists() ){
            assertTrue( correct.exists());
        }
        
        String correctOut = _clean( StreamUtil.readFully( correct ) );

        assertClose( correctOut , outString );
    }

    static String _clean( String s ){
        s = s.replaceAll( "tempFunc_\\d+_" , "tempFunc_" );
        return s;
    }
}
