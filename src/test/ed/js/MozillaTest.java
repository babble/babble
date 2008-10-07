package ed.js;

import java.io.*;
import org.testng.annotations.Test;
import ed.*;
import ed.appserver.*;
import ed.js.*;
import ed.js.engine.*;
import ed.js.func.*;
import ed.io.*;

public class MozillaTest extends TestCase {

    static String baseDir = "";
    static {
        try {
            baseDir = (new File( "/data/qa/modules/mozilla" )).getCanonicalPath();
        }
        catch( IOException e ) {
            e.printStackTrace();
        }
    }

    public MozillaTest(){
        this( new File( baseDir ) );
    }

    public MozillaTest( File dir ) {
        addFileTests( dir );
    }

    public void addFileTests( File dir ) {
        for ( File f : dir.listFiles() ){
	    if ( f.isDirectory() ) {
                addFileTests( f );
            }
            else if ( !f.getName().equals( "shell.js" ) && 
                      !f.getName().equals( "template.js" ) &&
                      !f.getName().equals( "jsref.js" ) &&
                      !f.getName().equals( "browser.js" ) && 
                      f.toString().endsWith( ".js" ) ) {
                add( new FileTest( f ) );
            }
	}
    }

    public static class FileTest extends TestCase {
        FileTest( File f ){
            super( f.toString() );
            _file = f;
        }

        public void runShell( Scope scope, File shellFile ) {
            try {
                Convert c = new Convert( shellFile );
                JSFunction f = c.get();
                ((JSCompiledScript)f).setPath( new JSFileLibrary( shellFile.getParentFile() , "local" , scope ) );
                f.call( scope );
            }
            catch ( Exception e ){
                e.printStackTrace();
            }
        }

        public void test()
            throws IOException {

            // set up scope
            Scope scope;
            scope = Scope.newGlobal().child( new File( "." ) );
            scope.setGlobal( true );
            scope.makeThreadLocal();

            // set it up to only print errors
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            final PrintStream out = new PrintStream( bout );

            JSFunction myout = new JSFunctionCalls1(){
                    public Object call( Scope scope ,Object o , Object extra[] ){
                        String str = JSInternalFunctions.JS_toString( o );
                        if( str.startsWith( " FAILED!" ) )
                            out.println( str );
                        return null;
                    }
                };
            scope.put( "print" , myout , true );

            // run all the shell.js scripts before the test
            // +1 for the final "/"
            String subpath = _file.getCanonicalPath().substring( baseDir.length() + 1 );
            String path[] = subpath.split( File.separator );
            String tempBase = baseDir + File.separator;

            runShell( scope, new File( tempBase + "shell.js" ));
            for( int i=0; i<path.length-1; i++ ) {
                tempBase = tempBase + path[i] + File.separator;
                runShell( scope, new File( tempBase + "shell.js" ) );
            }

            // now run the actual test
            Convert c = new Convert( _file );
            JSFunction f = c.get();
	    ((JSCompiledScript)f).setPath( new JSFileLibrary( _file.getParentFile() , "local" , scope ) );
            f.call( scope );

            String output = bout.toString().trim();
            try {
                assertEmptyString( output );
            }
            catch( MyAssert a ) {
                throw a;
            }
        }
        
        final File _file;
    }

    public static void main( String args[] ){
        MozillaTest ct;
        if( args.length > 0 ) {
            System.out.println( "running a subset of tests: "+baseDir );
            ct = new MozillaTest( new File( baseDir + File.separator + args[0] ) );
        }
        else {
            ct = new MozillaTest();
        }
        ct.runConsole();
    }
}
