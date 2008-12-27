// OptionsTest.java

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
import ed.appserver.*;

public class OptionsTest extends TestCase {

    @Test
    public void testLoopingConstructs1(){
        
        final String s = "while(1){}";
        final CompileOptions options = new CompileOptions();
        
        Convert.makeAnon( s , true , options );

        options.allowLoopingConstructs( false );
        Exception e = null;
        try {
            Convert.makeAnon( s , true , options );
        }
        catch ( Exception ee ){
            e = ee;
        }
        assert( e != null );
    }

    @Test
    public void testLoopingConstructs2(){
        
        final String s = "for(a=1; a<10; a++){}";
        final CompileOptions options = new CompileOptions();
        
        Convert.makeAnon( s , true , options );

        options.allowLoopingConstructs( false );
        Exception e = null;
        try {
            Convert.makeAnon( s , true , options );
        }
        catch ( Exception ee ){
            e = ee;
        }
        assert( e != null );
    }


    @Test
    public void testLoopingConstructs3(){

        final String s = "for( a in z ){}";
        final CompileOptions options = new CompileOptions();
        
        Convert.makeAnon( s , true , options );

        options.allowLoopingConstructs( false );
        Convert.makeAnon( s , true , options );
    }

    @Test
    public void testLoopingConstructs4(){

        final String s = "for each ( a in z ){}";
        final CompileOptions options = new CompileOptions();
        
        Convert.makeAnon( s , true , options );

        options.allowLoopingConstructs( false );
        Convert.makeAnon( s , true , options );
    }



    public static void main( String args[] ){
        (new OptionsTest()).runConsole();
    }
}
