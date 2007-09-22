// JSFunctionBaseGenerator.java

package ed.js;

public class JSFunctionBaseGenerator {
    
    static String _i( int i ){
        String s = "";
        while ( i > 0 ){
            s += "    ";
            i--;
        }
        return s;
    }

    public static void main( String args[] )
        throws Exception {
        
        StringBuilder buf = new StringBuilder();
        buf.append( "// JSFunctionBase.java\n\n" );
        buf.append( "package ed.js;\n\n" );
        buf.append( "public abstract class JSFunctionBase extends JSInternalFunctions { \n" );
        
        buf.append( _i( 1 ) + "public JSFunctionBase( int num ){\n" );
        buf.append( _i( 2 ) + "_num = num;\n" );
        buf.append( _i( 1 ) + "}\n\n" );
        buf.append( _i( 1 ) + "final int _num;\n\n" );

        int max = 30;

        for ( int i=0; i<=max; i++ ){
            buf.append( _i( 1 ) + "public Object call(" );
            for ( int j=0; j<i; j++ ){
                if ( j > 0 )
                    buf.append( " , " );
                buf.append( " Object p" + j );
            }
            buf.append( " ){\n" );
            
            buf.append( _i( 2 ) + "if ( _num == " + i + " )\n" + _i( 3 ) + "throw new RuntimeException( \"this should not happen\" );\n" );
            buf.append( _i( 2 ) + "if ( _num < " + i + " )\n" + _i( 3 ) + "throw new RuntimeException( \"too many params\" );\n" );

            if ( i == max ){
                buf.append( _i(2) + "throw new RuntimeException( \"fuck\" );\n " );
            }
            else {
                buf.append( _i( 2 ) + "return call(" );
                for ( int j=0; j<i; j++ )
                    buf.append( "p" + j + "," );
                buf.append( "null" );
                buf.append( ");\n" );
                
                buf.append( _i( 1 ) + "\n" );
            }
            buf.append( _i(1) + "}\n" );
        }

        buf.append( "\n}\n" );

        java.io.FileOutputStream fout = new java.io.FileOutputStream( "src/main/ed/js/JSFunctionBase.java" );
        fout.write( buf.toString().getBytes() );
        fout.close();

        System.out.println( buf );
    }
}
