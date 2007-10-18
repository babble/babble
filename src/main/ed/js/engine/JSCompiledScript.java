// JSCompiledScript.java

package ed.js.engine;

import java.util.*;

import org.mozilla.javascript.*;

import ed.util.*;
import ed.js.func.*;

public abstract class JSCompiledScript extends JSFunctionCalls0 {
 
    protected abstract Object _call( Scope scope , Object extra[] );

    public Object call( Scope scope , Object extra[] ){
        try {
            return _call( scope, extra );
        }
        catch ( RuntimeException re ){
            StackTraceElement stack[] = re.getStackTrace();
            for ( int i=0; i<stack.length; i++ ){

                StackTraceElement element = stack[i];
                if ( element == null )
                    continue;
                final String file = _convert.getClassName() + ".java";
                
                String es = element.toString();

                if ( ! es.contains( file ) )
                    continue;
                
                int line = StringParseUtil.parseInt( es.substring( es.lastIndexOf( ":" ) + 1 ) , -1 ) - _convert._preMainLines;
                List<Node> nodes = _convert._javaCodeToLines.get( line );
                if ( nodes == null )
                    continue;
                
                // the +1 is for the way rhino stuff
                line = _convert._nodeToSourceLine.get( nodes.get(0) ) + 1;
                
                System.out.println( nodes );
                System.out.println( "line:" + line );
                System.out.println( element );
                
            }
            throw re;
        }
    }

    Convert _convert;
}
