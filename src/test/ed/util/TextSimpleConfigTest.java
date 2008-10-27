// TextSimpleConfigTest.java

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

package ed.util;

import java.util.*;

import org.testng.annotations.Test;

import ed.*;

public class TextSimpleConfigTest extends TestCase {

    @Test
    public void test1(){
        
        final String data = 
            "a b\n" + 
            "  c : d \n" +
            "  e : f \n" +
            "a n\n" + 
            "  o\n" + 
            "r s t\n" +
            "r s u\n";

        
        TextSimpleConfig config = TextSimpleConfig.readString( data );
        assertEquals( 2 , config.getTypes().size() );
        assertEquals( "[a, r]" , config.getTypes().toString() );
        
        assertEquals( 2 , config.getNames( "a" ).size() );
        assertEquals( "[b, n]" , config.getNames( "a" ).toString() );

        assertEquals( 1 , config.getNames( "r" ).size() );
        assertEquals( "[s]" , config.getNames( "r" ).toString() );

        assertEquals( "{c=d, e=f}" , config.getMap( "a" , "b" ) );
        assertEquals( "[o]" , config.getValues( "a" , "n" ) );

        try {
            config.getValues( "a" , "b" );
            assert( false );
        }
        catch ( Exception e ){}

        try {
            config.getMap( "a" , "n" );
            assert( false );
        }
        catch ( Exception e ){}
        
        assertEquals( "[t, u]" , config.getValues( "r" , "s" ) );
    }
    
    @Test
    public void testOutput1(){
        final String data = 
            "a b\n" + 
            "  c : d \n" +
            "  e : f \n" +
            "a n\n" + 
            "  o\n" + 
            "r s\n" +
            "    t\n" + 
            "    u\n" ;

        final String data2 = 
            "a b\n" + 
            "  c : d \n" +
            "  e : f \n" +
            "a n\n" + 
            "  o\n" + 
            "r s t\n" +
            "r s u\n";

        assertClose( data , TextSimpleConfig.readString( data ).outputToString() );
        assertClose( TextSimpleConfig.readString( data ).outputToString() , 
                     TextSimpleConfig.readString( data2 ).outputToString() );
    }

    public static void main( String args[] )
        throws Exception {
        (new TextSimpleConfigTest()).runConsole();
    }
}
