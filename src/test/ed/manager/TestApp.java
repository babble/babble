// TestApp.java

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

package ed.manager;

public class TestApp {

    public static void main( String args[] )
        throws Exception {
        System.out.println( "hello" );
        Thread.sleep( 1000 );
        System.out.println( "goodbye" );
        System.out.flush();
        
        String s = "asdsaD";
        for ( int i=0; i<10; i++ ){
            s += s + s;
            System.gc();
        }

        System.err.println( "done" ); 
    }
    
}
