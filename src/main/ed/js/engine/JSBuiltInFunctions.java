// JSBuiltInFunctions.java

package ed.js.engine;

import ed.js.*;

public class JSBuiltInFunctions {

    public static class print extends JSFunction {
        print(){
            super( null , "print" , 1 );
        }

        public Object call( Scope scope , Object foo ){
            System.out.println( foo );
            return null;
        }
    }

    public static class NewObject extends JSFunction {
        NewObject(){
            super( null , "Object" , 0 );
        }

        public Object call( Scope scope ){
            return new JSObject();
        }
    }

    public static class NewArray extends JSFunction {
        NewArray(){
            super( null , "Array" , 0 );
        }

        public Object call( Scope scope ){
            return new JSArray();
        }
    }

    static Scope _myScope = new Scope( "Built-Ins" , null );
    static {
        _myScope.put( "print" , new print() , true );
        _myScope.put( "SYSOUT" , new print() , true );

        _myScope.put( "Object" , new NewObject() , true );
        _myScope.put( "Array" , new NewArray() , true );
    }
    
}
