// LineTest.java

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
                final String matchString = "_string(" + stringid + ")";
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
