// JSBuiltInFunctions.java

package ed.js.engine;

import ed.js.*;

public class JSBuiltInFunctions {

    public static class print extends JSFunction {
        print(){
            super( "print" , 1 );
        }

        public Object call( Object foo ){
            System.out.println( foo );
            return null;
        }
    }

    static Scope _myScope = new Scope( "Built-Ins" , null );
    static {
        _myScope.put( "print" , new print() , true );
        _myScope.put( "SYSOUT" , new print() , true );
    }
    
}
