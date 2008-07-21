// JSFileLibraryTest.java

/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package ed.appserver;

import java.io.*;

import org.testng.annotations.Test;

import ed.*;
import ed.js.*;
import ed.js.engine.*;

public class JSFileLibraryTest extends ed.TestCase {
    
    public JSFileLibraryTest(){
        scope = Scope.newGlobal().child();
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
        assertEquals( 6 , scope.eval( "local.libtest.foo.foo()" ) );
    }

    @Test(groups = {"basic"})
    public void testDirPath(){
        assertEquals( 5 , scope.eval( "local.getFromPath('libtest/foo')()" ) );
        assertEquals( 6 , scope.eval( "local.getFromPath('libtest/foo/foo')()" ) );
    }

    final Scope scope;
    final File root;
    final JSFileLibrary local;

    public static void main( String args[] ){
        (new JSFileLibraryTest()).runConsole();
    }
}
