// ConvertTest.java

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

package ed.js.engine;

import java.io.*;

import org.testng.annotations.Test;

import ed.*;
import ed.js.*;
import ed.js.func.*;
import ed.io.*;

public class ConvertTest extends TestCase {

    public ConvertTest(){
        
        File dir = new File( "src/test/ed/js/engine/" );
        for ( File f : dir.listFiles() )
            if ( f.toString().endsWith( ".js" ) )
                add( new FileTest( f ) );
        
        _scope = Scope.newGlobal().child( new File(".") );
    }
    
    @Test
    public void testMakeAnon(){
        assertClose( "5" , _makeAnon( "return 5;" ).toString() );
        assertClose( "6" , _makeAnon( "function(){ return 6; }" ).toString() );
    }

    @Test
    public void testFixStack(){

        JSFunction f = Convert.makeAnon( "x = {};\nx.a.b = 5;\n" );
        try {
            f.call( _scope.child() );
        }
        catch ( Exception e ){
            assertEquals( 2 , e.getStackTrace()[0].getLineNumber() );
        }
        
    }

    @Test
    public void testCleanName(){
        assertEquals( "abc_123" , Convert.cleanName( "abc/123" ) );
        assertEquals( "abc__123" , Convert.cleanName( "abc//123" ) );
        assertEquals( "abc__123" , Convert.cleanName( "/abc//123" ) );
    }

    Object _makeAnon( String code ){
        return Convert.makeAnon( code ).call( _scope );
    }

    public static class FileTest extends TestCase {
        FileTest( File f ){
            super( f.toString() );
            _file = f;
        }

        public void test()
            throws IOException {
            Convert c = new Convert( _file );

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            final PrintStream out = new PrintStream( bout );
            
            JSFunction f = c.get();
            Scope scope = Scope.newGlobal().child( new File( "." ) );
            scope.setGlobal( true );
            scope.makeThreadLocal();

            if ( _file.toString().contains( "/engine/" ) ){
                JSFunction myout = new JSFunctionCalls1(){
                        public Object call( Scope scope ,Object o , Object extra[] ){
                            out.println( JSInternalFunctions.JS_toString( o ) );
                            return null;
                        }
                    };
                
                scope.put( "print" , myout , true );
                scope.put( "SYSOUT" , myout , true );
            }

            f.call( scope );


            String outString = _clean( bout.toString() );
            
            if ( _file.toString().contains( "/engine/" ) ){
                File correct = new File( _file.toString().replaceAll( ".js$" , ".out" ) );
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
        }
        
        final File _file;
    }

    static String _clean( String s ){
        s = s.replaceAll( "tempFunc_\\d+_" , "tempFunc_" );
        return s;
    }

    final Scope _scope;
    
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
