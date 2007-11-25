// JSBuiltInFunctions.java

package ed.js.engine;

import ed.js.*;
import ed.js.func.*;

public class JSBuiltInFunctions {

    public static class print extends JSFunctionCalls1 {
        print(){
            super();
        }

        public Object call( Scope scope , Object foo , Object extra[] ){
            System.out.println( foo );
            return null;
        }
    }

    public static class NewObject extends JSFunctionCalls0 {
        NewObject(){
            super();
        }

        public Object call( Scope scope , Object extra[] ){
            return new JSObjectBase();
        }
    }

    public static class NewArray extends JSFunctionCalls1 {
        NewArray(){
            super();
        }

        public Object call( Scope scope , Object a , Object[] extra ){
            int len = 0;
            if ( extra == null || extra.length == 0 ){
                if ( a instanceof Number )
                    len = ((Number)a).intValue();
            }
            else {
                len = 1 + extra.length;
            }
            
            JSArray arr = new JSArray( len );
            
            if ( extra != null && extra.length > 0 ){
                arr.setInt( 0 , a );
                for ( int i=0; i<extra.length; i++)
                    arr.setInt( 1 + i , extra[i] );
            }
            
            return arr;
        }
    }
    
    public static class NewDate extends JSFunctionCalls1 {
        public Object call( Scope scope , Object t , Object extra[] ){
            
            if ( t == null )
                return new JSDate();
            
            if ( ! ( t instanceof Number ) )
                return new JSDate();
            
            return new JSDate( ((Number)t).longValue() );
        }
    }

    public static class CrID extends JSFunctionCalls1 {
        public Object call( Scope scope , Object idString , Object extra[] ){
            return new ed.db.ObjectId( idString.toString() );
        }
    }

    static Scope _myScope = new Scope( "Built-Ins" , null );
    static {
        _myScope.put( "print" , new print() , true );
        _myScope.put( "SYSOUT" , new print() , true );

        _myScope.put( "Object" , new NewObject() , true );
        _myScope.put( "Array" , new NewArray() , true );
        _myScope.put( "Date" , JSDate._cons , true );
        _myScope.put( "String" , JSString._cons , true );
        _myScope.put( "RegExp" , JSRegex._cons , true );

        _myScope.put( "Math" , JSMath.getInstance() , true );
        
        _myScope.put( "CrID" , new CrID() , true );

        _myScope.put( "Base64" , new ed.util.Base64() , true );
        
        _myScope.put( "parseBool" , new JSFunctionCalls1(){
                public Object call( Scope scope , Object b , Object extra[] ){
                    if ( b == null )
                        return false;
                    
                    String s = b.toString();
                    if ( s.length() == 0 )
                        return false;
                    
                    char c = s.charAt( 0 );
                    return c == 't' || c == 'T';
                }
            } , true );
        

        JSON.init( _myScope );
    }
    
}
