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

    @Test(groups = {"basic"})    
    public void testBasic(){
        AppRequest r = new AppRequest( CONTEXT , HttpRequest.getDummy( "/silly" , "Host: www.foo.com" )  );
        URLFixer f = new URLFixer( r._request , r );


        assertClose( "/1.jpg?ctxt=nullnull&lm=" + one.lastModified() , f.fix( "NOCDN/1.jpg" ) );
        assertClose( "/1.jpg?ctxt=nullnull&lm=" + one.lastModified() , f.fix( "NOCDN1.jpg" ) );
        assertClose( "http://static.10gen.com/www.foo.com/1.jpg?ctxt=nullnull&lm=" + one.lastModified() , f.fix( "/1.jpg" ) );
        assertClose( "http://static.10gen.com/www.foo.com/1.jpg?ctxt=nullnull&lm=" + one.lastModified() , f.fix( "1.jpg" ) );
    }

    @Test(groups = {"basic"})    
    public void testCDN1(){
        AppRequest r = new AppRequest( CONTEXT , HttpRequest.getDummy( "/www.foo.com/silly.css" , "Host: static.10gen.com" ) , "www.foo.com" , "/silly.css" );
        URLFixer f = new URLFixer( r._request , r );
        assertClose( "http://static.10gen.com/www.foo.com/1.jpg?ctxt=nullnull&lm=" + one.lastModified() , f.fix( "/1.jpg" ) );
        assertClose( "http://static.10gen.com/www.foo.com/1.jpg?ctxt=nullnull&lm=" + one.lastModified() , f.fix( "1.jpg" ) );
    }    
    
    @Test(groups = {"basic"})    
    public void testCDN2(){
        AppRequest r = new AppRequest( CONTEXT , HttpRequest.getDummy( "/www.foo.com/silly.css" , "Host: www.bar.com\n" ) );
        URLFixer f = new URLFixer( r._request , r );
        assertClose( "http://static.10gen.com/www.bar.com/1.jpg?ctxt=nullnull&lm=" + one.lastModified() , f.fix( "/1.jpg" ) );
        assertClose( "http://static.10gen.com/www.bar.com/www.foo.com/1.jpg?ctxt=nullnull&lm=doesntexist" , f.fix( "1.jpg" ) );
    }

    @Test(groups = {"basic"})    
    public void testAlreadyTweaked1(){
        AppRequest r = new AppRequest( CONTEXT , HttpRequest.getDummy( "/www.foo.com/silly.css" , "Host: www.bar.com\n" ) );
        URLFixer f = new URLFixer( r._request , r );
        assertClose( "http://static.10gen.com/www.bar.com/www.foo.com/1.jpg?ctxt=abc&lm=doesntexist" , f.fix( "1.jpg?ctxt=abc" ) );
        assertClose( "http://static.10gen.com/www.bar.com/www.foo.com/1.jpg?ctxt=abc&lm=1" , f.fix( "1.jpg?ctxt=abc&lm=1" ) );
        assertClose( "http://static.10gen.com/www.bar.com/www.foo.com/1.jpg?lm=1&ctxt=nullnull" , f.fix( "1.jpg?lm=1" ) );
    }    
    
    public static void main( String args[] ){
        (new URLFixerTest()).runConsole();
    }
}
