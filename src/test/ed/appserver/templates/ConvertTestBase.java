// ConvertTestBase.java

package ed.appserver.templates;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.testng.annotations.*;

import ed.*;
import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.appserver.JSFileLibrary;
import ed.io.*;

public abstract class ConvertTestBase extends TestCase {

    static final boolean DEBUG = Boolean.getBoolean( "DEBUG.TEMPLATES" );

    ConvertTestBase( String extension ){
        this( extension , null );
    }

    ConvertTestBase( String extension , String args[] ){

        _extension = extension;
        
        _all = new ArrayList<FileTest>();
        
        if ( args != null ){
            for ( int i=0; i<args.length; i++ ){
                if ( args[i].startsWith( "-" ) )
                    continue;
                FileTest ft = new FileTest( new File( args[i] ) );
                add( ft );
                _all.add( ft );
            }
        }
        
        if ( _all.size() == 0 ){
            File dir = new File( "src/test/ed/appserver/templates/" );
            for ( File f : dir.listFiles() )
                if ( f.toString().endsWith( _extension ) ){
                    FileTest ft = new FileTest( f );
                    add( ft );
                    _all.add( ft );
                }
        }
        
    }
    
    abstract TemplateConverter getConverter();
    
    Object[] getArgs(){
        return null;
    }

    public class FileTest extends TestCase {

        FileTest( File f ){
            super( f.toString() );
            _file = f;
        }

        @Test
        public void test()
            throws Exception {
            
            if ( DEBUG ) {
                System.out.println( "*********************");
                System.out.println( _file );
                System.out.println( "*********************");
            }

            System.out.println("ConvertTestBase : testing = " + _file);

            final String in = StreamUtil.readFully( new FileInputStream( _file ) );

            Template t = new Template( _file.getAbsolutePath() , in );
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
            
            scope.put( "local" , new JSFileLibrary( new File( "src/test/ed/appserver/templates/" ) ,  "local" , scope ) , true );

            func.call( scope , getArgs() );
            
            String got = _clean( output.toString() );
            if ( DEBUG ) 
                System.out.println( got );
            

            File resultFile = new File( _file.getAbsolutePath().replaceAll( _extension + "$" , ".out" ) );
            if ( ! resultFile.exists() )
                resultFile = new File( _file.getAbsolutePath() + ".out" );
            String expected = _clean( StreamUtil.readFully( new FileInputStream( resultFile ) ) );
            
            assertClose( expected , got, "Error : " + _file + " : " );

            Matcher m = Pattern.compile( "LINE(\\d+)" ).matcher( in );
            while ( m.find() ){
                final String tofind = m.group();
                final int lineNumber = Integer.parseInt( m.group(1) );
                
                assertNotNull( r.getLineMapping() );

                final String newLines[] = r.getNewTemplate().getContent().split( "\n" );
                int where = 0;
                for ( ; where<newLines.length; where++ )
                    if ( newLines[where].contains( tofind ) )
                        break;
                assertTrue( where < newLines.length );
                
                assertEquals( lineNumber , (int)(r.getLineMapping().get( where + 1 ) ) );
            }
        }

        final File _file;
    }

    final String _extension;
    final List<FileTest> _all;

    static String _clean( String s ){
        s = s.replaceAll( "[\\s\r\n]+" , "" );
        s = s.replaceAll( " +>" , ">" );
        return s;
    }

}
