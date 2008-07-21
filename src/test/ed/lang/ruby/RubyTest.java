// RubyTest.java

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

package ed.lang.ruby;

import java.io.*;

import ed.*;
import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.io.*;

public class RubyTest extends TestCase {

    public RubyTest(){
        
        File dir = new File( "src/test/ed/lang/ruby/" );
        for ( File f : dir.listFiles() )
            if ( f.toString().endsWith( ".rb" ) )
                add( new RubyFileTest( f ) );
        
    }
    
    public static class RubyFileTest extends TestCase {
        RubyFileTest( File f ){
            super( f.toString() );
            _file = f;
        }

        public void test()
            throws IOException {
            RubyConvert c = new RubyConvert( _file );

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            final PrintStream out = new PrintStream( bout );
            
            JSFunction f = c.get();
            Scope scope = Scope.getAScope().getGlobal().child();
            scope.setGlobal( true );
            
            JSFunction myout = new JSFunctionCalls1(){
                    public Object call( Scope scope ,Object o , Object extra[] ){
                        out.println( o );
                        return null;
                    }
                };
            
            scope.put( "print" , myout , true );

            
            scope.makeThreadLocal();
            f.call( scope );
            scope.kill();


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
                all.add( new RubyFileTest( new File( s ) ) );
            all.runConsole();
        }
        else {
            RubyTest rt = new RubyTest();
            rt.runConsole();
        }
    }

}
