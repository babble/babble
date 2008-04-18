// RubyTest.java

package ed.lang.ruby;

import java.io.*;

import org.testng.annotations.Test;

import ed.*;
import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.io.*;
import ed.lang.ruby.*;

public class RubyTest extends TestCase {

    public RubyTest(){
        
        File dir = new File( "src/test/ed/lang/ruby/" );
        for ( File f : dir.listFiles() )
            if ( f.toString().endsWith( ".rb" ) )
                add( new FileTest( f ) );
        
    }
    
    public static class FileTest extends TestCase {
        FileTest( File f ){
            super( f.toString() );
            _file = f;
        }

        public void test()
            throws IOException {
            RubyConvert c = new RubyConvert( _file );

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            final PrintStream out = new PrintStream( bout );
            
            JSFunction f = c.get();
            Scope scope = Scope.GLOBAL.child();
            
            JSFunction myout = new JSFunctionCalls1(){
                    public Object call( Scope scope ,Object o , Object extra[] ){
                        out.println( o );
                        return null;
                    }
                };
            
            scope.put( "print" , myout , true );
            scope.put( "SYSOUT" , myout , true );


            f.call( scope );


            String outString = _clean( bout.toString() );
            
            File correct = new File( _file.toString().replaceAll( ".rb$" , ".out" ) );
            if ( ! correct.exists() ){
                assertTrue( correct.exists() );
            }
            String correctOut = _clean( StreamUtil.readFully( correct ) );
            
            try {
                assertClose( correctOut , outString );
            }
            catch ( MyAssert a ){
                System.out.println();
                System.out.println( correctOut.replaceAll( "[\r\n ]+" , " " ) );
                System.out.println( outString.replaceAll( "[\r\n ]+" , " " ) );
                throw a;
            }
        }
        
        final File _file;
    }
    
    static String _clean( String s ){
        s = s.replaceAll( "tempFunc_\\d+_" , "tempFunc_" );
        return s;
    }
    
    public static void main( String args[] ){
        if ( args.length > 0 ){
            TestCase all = new TestCase();
            for ( String s : args )
                all.add( new FileTest( new File( s ) ) );
            all.runConsole();
        }
        else {
            ConvertTest ct = new ConvertTest();
            ct.runConsole();
        }
    }

}
