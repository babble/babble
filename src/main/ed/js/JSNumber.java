// JSNumber.java

package ed.js;

import java.util.*;

import ed.js.func.*;
import ed.js.engine.*;

/**
   i'm not really sure what to do with this
   right now its just holder for weird stuff
 */
public class JSNumber {


    private static Map<String,JSFunction> functions = new HashMap<String,JSFunction>();
    public static JSFunction getFunction( String name ){
        return functions.get( name );
    }
    
    public static JSFunction toFixed = new JSFunctionCalls1(){
            public Object call( Scope sc , Object pointsInt , Object foo[] ){
                Number n = (Number)(sc.getThis());
                int num = 0;
                if ( pointsInt != null && pointsInt instanceof Number )
                    num = ((Number)pointsInt).intValue();
                
                String s = n.toString();
                int idx = s.indexOf( "." );
                
                if ( idx < 0 ){
                    if( num > 0 ){
                        s += ".";
                        while( num > 0 ){
                            s += "0";
                            num--;
                        }
                    }
                    return s;
                }

                if ( s.length() - idx < num ){ // need more
                    int toAdd = ( s.length() - idx ) - num;
                    for ( int i=0; i<toAdd; i++ )
                        s += "0";
                    return s;
                }
                
                if ( num == 0 )
                    return s.substring( 0 , idx  );
                return s.substring( 0 , idx + 1 + num  );
            }
        };
    static {
        functions.put( "toFixed" , toFixed );
    }
}

