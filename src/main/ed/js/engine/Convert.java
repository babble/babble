// Convert.java

package ed.js.engine;

public class Convert {
    
    String convertFunction( String s ){
        return s;
    }
    
    static String convertStatement( String s ){
        s = s.replaceAll( "\\bvar\\b" , "Object" );
        s = s.replaceAll( "\\breturn\\s*;" , "return null;" );
        return s;
    }

    public static void main( String args[] )
        throws Exception {
        
        
        
    }
    
}
