// CSSFixerTest.java

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

import org.testng.annotations.Test;

public class CSSFixerTest extends ed.TestCase {

    CSSFixer fixer = new CSSFixer( new URLFixer( "P" , "S=1" , null ) );

    @Test(groups = {"basic"})    
    public void testBasic(){
        assertClose( "body { color: red; }" , fixer.fixSingeLine( "body { color: red; }" ) );
        assertClose( "body { color: red; url(P/a.jpg?S=1&); }" , fixer.fixSingeLine( "body { color: red; url(/a.jpg); }" ) );
        assertClose( "body { color: red; url(P/a.jpg?z=1&S=1&); }" , fixer.fixSingeLine( "body { color: red; url(/a.jpg?z=1); }" ) );
    }

    public static void main( String args[] ){
        (new CSSFixerTest()).runConsole();
    }
}
