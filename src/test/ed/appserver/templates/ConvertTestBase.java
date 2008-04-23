// ConvertTestBase.java

package ed.appserver.templates;

import java.io.*;

import org.testng.annotations.Test;

import ed.*;
import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.io.*;

public abstract class ConvertTestBase extends TestCase {

    ConvertTestBase( String extension ){

        _extension = extension;

        File dir = new File( "src/test/ed/appserver/templates/" );
        for ( File f : dir.listFiles() )
            if ( f.toString().endsWith( _extension ) )
                add( new FileTest( f ) );
        
    }

    abstract TemplateConverter getConverter();

    public class FileTest extends TestCase {

        FileTest( File f ){
            super( f.toString() );
            _file = f;
        }

        public void test()
            throws Exception {
            
            Template t = new Template( _file.getAbsolutePath() , StreamUtil.readFully( new FileInputStream( _file ) ) );
            TemplateConverter.Result r = (getConverter()).convert( t );

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
            

            File resultFile = new File( _file.getAbsolutePath().replaceAll( _extension + "$" , ".out" ) );
            String expected = _clean( StreamUtil.readFully( new FileInputStream( resultFile ) ) );
            
            assertClose( expected , got );
        }

        final File _file;
    }

    final String _extension;

    static String _clean( String s ){
        s = s.replaceAll( "[\\s\r\n]+" , "" );
        s = s.replaceAll( " +>" , ">" );
        return s;
    }

}
