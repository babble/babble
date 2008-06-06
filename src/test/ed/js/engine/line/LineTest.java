// LineTest.java

package ed.js.engine.line;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.testng.annotations.Test;
import org.testng.annotations.Factory;

import ed.*;
import ed.io.*;
import ed.js.*;
import ed.js.engine.*;

public class LineTest extends TestCase {

    public LineTest(){
        File dir = new File( "src/test/ed/js/engine/line/" );
        for ( File f : dir.listFiles() ){
            if ( f.toString().endsWith( ".js" ) ){
                OneLineTest t = new OneLineTest( f );
                add( t );
                _all.add( t );
            }
        }
    }

    @Factory
    public Object[] createTestInstances(){
        return _all.toArray();
    }

    public static class OneLineTest extends TestCase {
        OneLineTest( File f ){
            super( f.toString() );
            _file = f;
        }

        @Test
        public void test()
            throws IOException {

            boolean debug = false;

            Convert c = new Convert( _file );
            JSFunction f = c.get();
            
            final String jsCode = StreamUtil.readFully( new FileInputStream( _file ) );
            final String javaCode = c.getClassString();
            final String javaLines[] = javaCode.split( "\n" );

            final Matcher m = Pattern.compile( "LINE_(\\d+)_" ).matcher( jsCode );
            while( m.find() ){
                final int line = Integer.parseInt( m.group(1) );
                if ( debug ) System.out.println( line );
                
                final int stringid = c.findStringId( m.group() );
                final String matchString = "_strings[" + stringid + "]";
                if ( debug ) System.out.println( "\t" + matchString );
                
                int j=0;
                for ( ; j<javaLines.length; j++)
                    if ( javaLines[j].contains( matchString ) )
                        break;
                j++;
                
                if ( debug ) System.out.println( "\t" + j );
                if ( line != c._mapLineNumber( j ) )
                    c._debugLineNumber( j );
                assertEquals( line , c._mapLineNumber( j ) );
            }
        }


        final File _file;
    }

    final List<OneLineTest> _all = new ArrayList<OneLineTest>();
    
    public static void main( String args[] ){
        if ( args.length > 0 ){
            TestCase all = new TestCase();
            for ( String s : args )
                all.add( new OneLineTest( new File( s ) ) );
            all.runConsole();
        }
        else {
            LineTest lt = new LineTest();
            lt.runConsole();
        }
    }
}
