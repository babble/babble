// JSFunctionBaseGenerator.java

package ed.js;

public class JSFunctionBaseGenerator {

    static int MAX = 28;
    
    static String _i( int i ){
        String s = "";
        while ( i > 0 ){
            s += "    ";
            i--;
        }
        return s;
    }

    static String _extraSymbol( int i ){
        if ( i == MAX )
            return "...";
        return "[]";
    }

    public static void doInterface()
        throws Exception {
        
        StringBuilder buf = new StringBuilder();
        buf.append( "//JSFunctionBase.java\n\n" );
        buf.append( "package ed.js;\n\n" );
        buf.append( "import ed.js.engine.Scope;\n" );
        buf.append( "public abstract class JSFunctionBase extends JSInternalFunctions { \n" );
        
        buf.append( _i( 1 ) + "public JSFunctionBase( int num ){\n" );
        buf.append( _i( 2 ) + "_num = num;\n" );
        buf.append( _i( 1 ) + "}\n\n" );
        buf.append( _i( 1 ) + "final int _num;\n\n" );

        for ( int i=0; i<=MAX; i++ ){
            
            // main one
            buf.append( _i( 1 ) + "public abstract Object call( Scope scope " );
            for ( int j=0; j<i; j++ )
                buf.append( " , Object p" + j );
            buf.append( " , Object " + _extraSymbol( i ) + " extra );\n" );
            
            // don't need one without extra for last b/c of varargs
            if ( i == MAX )
                break;

            // no extra
            buf.append( _i( 1 ) + "public Object call( Scope scope " );
            for ( int j=0; j<i; j++ )
                buf.append( " , Object p" + j );
            buf.append( " ){\n" );
            buf.append( _i(2) + "return call( scope" );
            for ( int j=0; j<i; j++ )
                buf.append( " , p" + j );
            buf.append( " , null );\n" + _i(1) + "}\n" );

        }

        buf.append( "\n}\n" );

        System.out.println( buf );

        java.io.FileOutputStream fout = new java.io.FileOutputStream( "src/main/ed/js/JSFunctionBase.java" );
        fout.write( buf.toString().getBytes() );
        fout.close();

    }

    public static void doFunc( int num )
        throws Exception {
        
        StringBuilder buf = new StringBuilder();
        buf.append( "//JSFunctionCalls" + num + ".java\n\n" );
        buf.append( "package ed.js.func;\n\n" );
        buf.append( "import ed.js.engine.*;\n" );
        buf.append( "import ed.js.*;\n" );
        buf.append( "public abstract class JSFunctionCalls" + num + " extends JSFunction { \n" );
        
        buf.append( _i( 1 ) + "public JSFunctionCalls" + num + "(){\n" );
        buf.append( _i( 2 ) + "super( " + num + " );\n" );
        buf.append( _i( 1 ) + "}\n\n" );

        buf.append( _i( 1 ) + "public JSFunctionCalls" + num + "( Scope scope , String name ){\n" );
        buf.append( _i( 2 ) + "super( scope , name , " + num + " );\n" );
        buf.append( _i( 1 ) + "}\n\n" );

        for ( int i=0; i<=MAX; i++ ){
            if ( i == num )
                continue;
            
            buf.append( _i( 1 ) + "public Object call( Scope scope " );
            for ( int j=0; j<i; j++ ){
                buf.append( " , Object p" + j );
            }
            buf.append( " , Object " + _extraSymbol( i ) + " extra ){\n" );
            
            buf.append( _i(3) + "if ( _lastStart.get() == null ) _lastStart.set( " + i + " ); \n" );

            if ( i < num ){
                for ( int j=i; j<num; j++ )
                    buf.append( _i(3) + "Object p" + j + " = extra == null || extra.length <= " + ( j - i ) + " ? null : extra[" + ( j - i ) + "];\n" );
                
                buf.append( _i(3) + "Object newExtra[] = extra == null || extra.length <= " + ( num - i ) + " ? null : new Object[ extra.length - " + ( num -i ) + "];\n" );
                buf.append( _i(3) + "if ( newExtra != null )\n" );
                buf.append( _i(4) + "for ( int i=0; i<newExtra.length; i++ )\n" );
                buf.append( _i(5) + "newExtra[i] = extra[i+" + ( num - i ) + "];\n" );
                
                buf.append( _i(3) + "return call( scope" );
                for ( int j=0; j<num; j++ )
                    buf.append( " , p" + j );
                buf.append( " , newExtra );\n" );
            }
            else {
                buf.append( _i(3) + "boolean needExtra = " );
                for ( int j=num; j<i; j++ ){
                    if ( j > num )
                        buf.append( " || " );
                    buf.append( " p" + j + " != null " );
                }
                buf.append( " || ( extra != null && extra.length > 0 ) ;\n" );
                
                buf.append( _i(3) + "Object newExtra[] = needExtra ? new Object[" + ( i - num ) + " + ( extra == null ? 0 : extra.length ) ] : null;\n" );
                
                
                buf.append( _i(3) + "if ( newExtra != null ){\n" );
                for ( int j=num; j<i; j++ )
                    buf.append( _i(4) + "newExtra[" + ( j - num ) + "] = p" + j + ";\n" );
                buf.append( _i(4) + "for ( int i=0; extra != null && i<extra.length; i++ )\n" );
                buf.append( _i(5) + "newExtra[i + " + ( i - num ) + "] = extra[i];\n" );
                buf.append( _i(3) + "}\n" );
                
                buf.append( _i(3) + "return call( scope" );
                for ( int j=0; j<num; j++ )
                    buf.append( " , p" + j );
                buf.append( " , newExtra );\n" );
            }
            
            
            buf.append( _i(1) + "}\n\n" );
            
        }

        buf.append( _i(1) + "protected ThreadLocal<Integer> _lastStart = new ThreadLocal<Integer>();\n" );

        buf.append( "\n}\n" );

        System.out.println( buf );

        java.io.FileOutputStream fout = new java.io.FileOutputStream( "src/main/ed/js/func/JSFunctionCalls" + num + ".java" );
        fout.write( buf.toString().getBytes() );
        fout.close();

    }
    
    

    public static void main( String args[] )
        throws Exception {
        
        for ( int i=0; i<=MAX; i++ ){
            doFunc( i );
        }

        doInterface();

    }
}
