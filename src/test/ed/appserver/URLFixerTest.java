// URLFixerTest.java

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

import ed.net.httpserver.*;

public class URLFixerTest extends ed.TestCase {

    AppContext CONTEXT = new AppContext( "src/test/samplewww" );
    File one = new File( "src/test/samplewww/1.jpg" );
    AppRequest REQUEST = new AppRequest( CONTEXT , HttpRequest.getDummy( "/silly" , "Host: www.foo.com" ) , "/silly" );
    URLFixer fixer = new URLFixer( REQUEST._request , REQUEST );

    @Test(groups = {"basic"})    
    public void testBasic(){
        assertClose( "/1.jpg?ctxt=nullnull&lm=" + one.lastModified() , fixer.fix( "NOCDN/1.jpg" ) );
        assertClose( "/1.jpg?ctxt=nullnull&lm=" + one.lastModified() , fixer.fix( "NOCDN1.jpg" ) );
        assertClose( "http://static.10gen.com/www.foo.com/1.jpg?ctxt=nullnull&lm=" + one.lastModified() , fixer.fix( "/1.jpg" ) );
        assertClose( "http://static.10gen.com/www.foo.com/1.jpg?ctxt=nullnull&lm=" + one.lastModified() , fixer.fix( "1.jpg" ) );
    }

    public static void main( String args[] ){
        (new URLFixerTest()).runConsole();
    }
}
