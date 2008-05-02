// JSFileLibraryTest.java

package ed.appserver;

import java.io.*;

import org.testng.annotations.Test;

import ed.*;
import ed.js.*;
import ed.js.engine.*;

public class JSFileLibraryTest extends ed.TestCase {
    
    public JSFileLibraryTest(){
        scope = Scope.GLOBAL.child();
        root = new File( "src/test/samplewww" );
        local = new JSFileLibrary( root , "local" , scope );
        scope.set( "local" , local );
    }

    @Test(groups = {"basic"})
    public void testSetup(){
        assertTrue( root.exists() );
    }

    @Test(groups = {"basic"})
    public void testBasic(){
        assertTrue( local.get( "base" ) instanceof JSFunction );
    }

    @Test(groups = {"basic"})
    public void testConflict(){
        try {
            assertTrue( local.get( "libtest" ) instanceof JSFileLibrary );
            ((JSFileLibrary)local.get( "libtest" )).get( "a" );
            assertTrue( false );
        }
        catch ( Throwable t ){
            assertTrue( t.toString().indexOf( "collision" ) > 0 );
        }
    }

    @Test(groups = {"basic"})
    public void testDirSameName(){
        assertEquals( 5 , scope.eval( "local.libtest.foo()" ) );
        assertEquals( 5 , scope.eval( "local.libtest.foo.foo()" ) );
    }

    final Scope scope;
    final File root;
    final JSFileLibrary local;

    public static void main( String args[] ){
        (new JSFileLibraryTest()).runConsole();
    }
}
