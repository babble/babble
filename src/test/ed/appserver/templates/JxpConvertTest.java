// JxpConvertTest.java

package ed.appserver.templates;

import java.io.*;

import org.testng.annotations.Test;

import ed.*;
import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.io.*;

public class JxpConvertTest extends TestCase {

    public JxpConvertTest(){

        File dir = new File( "src/test/ed/appserver/templates/" );
        for ( File f : dir.listFiles() )
            if ( f.toString().endsWith( ".jxp" ) )
                add( new FileTest( f ) );
        
    }

    public static class FileTest extends TestCase {

        FileTest( File f ){
            super( f.toString() );
            _file = f;
        }

        public void test()
            throws Exception {
            
            Template t = new Template( _file.getAbsolutePath() , StreamUtil.readFully( new FileInputStream( _file ) ) );
            TemplateConverter.Result r = (new JxpConverter()).convert( t );

            assertNotNull( r );
            
            Convert c = new Convert( _file.toString() , r.getNewTemplate().getContent() );
            JSFunction func = c.get();
            
            Scope scope = Scope.GLOBAL.child();
            
            final StringBuilder output = new StringBuilder();
            
            JSFunction myout = new JSFunctionCalls1(){
                    public Object call( Scope scope ,Object o , Object extra[] ){
                        output.append( o ).append( "\n" );
                        return null;
                    }
                };
            
            scope.put( "print" , myout , true );
            scope.put( "SYSOUT" , myout , true );
            
            func.call( scope );
            
            String got = _clean( output.toString() );
            System.out.println( got );
            

            File resultFile = new File( _file.getAbsolutePath().replaceAll( ".jxp$" , ".out" ) );
            String expected = _clean( StreamUtil.readFully( new FileInputStream( resultFile ) ) );
            
            assertClose( expected , got );
        }

        final File _file;
    }

    static String _clean( String s ){
        s = s.replaceAll( "[\\s\r\n]+" , "" );
        s = s.replaceAll( " +>" , ">" );
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
            JxpConvertTest t = new JxpConvertTest();
            t.runConsole();
        }
    }
            
}
