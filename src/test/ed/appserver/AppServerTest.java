// AppServerTest.java

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

import static ed.appserver.AppContextHolder.Info;

public class AppServerTest extends ed.TestCase {

    String _root = "src/test/data/";

    @Test(groups = {"basic"})
    public void testGetContext(){
        AppContextHolder as = new AppContextHolder( _root + "admin" , _root );

        assertEquals( _root + "alleyinsider" , as.getContext( "alleyinsider.latenightcoders.com" , ""  ).getRoot() );
        assertEquals( _root + "alleyinsider" , as.getContext( "alleyinsider.com" , ""  ).getRoot() );
        
        assertEquals( _root + "admin" , as.getContext( "sb1.latenightcoders.com" , ""  ).getRoot() );
        assertEquals( _root + "admin" , as.getContext( "latenightcoders.com" , ""  ).getRoot() );
        assertEquals( _root + "admin" , as.getContext( "" , ""  ).getRoot() );
        
        assertEquals( _root + "a/www" , as.getContext( "a.com" , ""  ).getRoot() );
        assertEquals( _root + "a/www" , as.getContext( "www.a.com" , ""  ).getRoot() );
        assertEquals( _root + "a/dev" , as.getContext( "dev.a.com" , ""  ).getRoot() );

        assertEquals( _root + "a/www||a.com||/" , as.getContext( "a.com" , ""  ).toString() );
        assertEquals( _root + "a/www||a.com||/" , as.getContext( "www.a.com" , ""  ).toString() );
        assertEquals( _root + "a/dev||dev.a.com||/" , as.getContext( "dev.a.com" , ""  ).toString() );
        
        

        assertEquals( _root + "alleyinsider||alleyinsider||/images/logo.gif" , as.getContext( "origin.10gen.com" , "/alleyinsider/images/logo.gif"  ).toString() );
        assertEquals( _root + "admin||zzz.com||/alleyinsider/images/logo.gif" , as.getContext( "zzz.10gen.com" , "/alleyinsider/images/logo.gif" ).toString() );

        assertEquals( _root + "www/www||www||/foo" , as.getContext( "www.10gen.com" , "/foo" ).toString() );
        assertEquals( _root + "www/www||www||/foo" , as.getContext( "www.www.10gen.com" , "/foo" ).toString() );
        assertEquals( _root + "www/www||www||/foo" , as.getContext( "www.www.10gen.com" , "/foo" ).toString() );
        assertEquals( _root + "www/www||www||/foo" , as.getContext( "www.10gen.com" , "/foo" ).toString() );
        assertEquals( _root + "www/www||www||/foo" , as.getContext( "www.www.10gen.com" , "/foo" ).toString() );
        
        assertEquals( _root + "www/stage||stage.www||/foo" , as.getContext( "stage.www.10gen.com" , "/foo" ).toString() );
        
        assertEquals( _root + "www/www||www||/foo" , as.getContext( "www.10gen.com" , "/foo" ).toString() );
        assertEquals( _root + "www/www||www||/foo" , as.getContext( "www.www.10gen.com" , "/foo" ).toString() );

        assertEquals( _root + "www/www||www||/foo" , as.getContext( "www.10gen.com" , "/foo" ).toString() );
        assertEquals( _root + "www/www||www||/foo" , as.getContext( "www.www.10gen.com" , "/foo" ).toString() );
        assertEquals( _root + "www/www||www||/foo" , as.getContext( "www.www.10gen.com" , "/foo" ).toString() );
        assertEquals( _root + "www/www||www||/foo" , as.getContext( "www.10gen.com" , "/foo" ).toString() );
        assertEquals( _root + "www/www||www||/foo" , as.getContext( "www.www.10gen.com" , "/foo" ).toString() );
        
        assertEquals( _root + "www/stage||stage.www||/foo" , as.getContext( "stage.www.10gen.com" , "/foo" ).toString() );
        
        assertEquals( _root + "www/www||www||/foo" , as.getContext( "www.10gen.com" , "/foo" ).toString() );
        assertEquals( _root + "www/www||www||/foo" , as.getContext( "www.www.10gen.com" , "/foo" ).toString() );

        assertEquals( _root + "www/stage||stage.www||/foo" , as.getContext( "origin.10gen.com" , "/stage.www.10gen.com/foo" ).toString() );

        assertEquals( _root + "www/www||www||/foo" , as.getContext( "origin.10gen.com" , "/www.10gen.com/foo" ).toString() );
        assertEquals( _root + "www/www||www||/foo" , as.getContext( "origin.10gen.com" , "/www.www.10gen.com/foo" ).toString() );
        
    }


    @Test(groups = {"basic"})
    public void testGetPossibleSiteNames(){    
        assertEquals( "[foo.com/abc, foo/abc]" , AppContextHolder.getPossibleSiteNames( "static.10gen.com" , "/www.foo.com/abc" ).toString() );
        assertEquals( "[foo.com/abc, foo/abc]" , AppContextHolder.getPossibleSiteNames( "origin.10gen.com" , "/www.foo.com/abc" ).toString() );
        assertEquals( "[foo.com/abc, foo/abc]" , AppContextHolder.getPossibleSiteNames( "static.10gen.com" , "/foo.com/abc" ).toString() );
        assertEquals( "[foo.co.uk/abc, foo/abc]" , AppContextHolder.getPossibleSiteNames( "static.10gen.com" , "/www.foo.co.uk/abc" ).toString() );
        
        assertEquals( "[abc.foo.com/, foo.com/, foo/]" , AppContextHolder.getPossibleSiteNames( "abc.foo.com" , "/" ).toString() );
        assertEquals( "[abc.foo.com/, foo.com/, foo/]" , AppContextHolder.getPossibleSiteNames( "abc.foo.com.10gen.com" , "/" ).toString() );

        assertEquals( "[abc.foo/, foo/]" , AppContextHolder.getPossibleSiteNames( "abc.foo.10gen.com" , "/" ).toString() );
    }
    
    @Test(groups = {"basic"})
    public void testFixBase(){    
        assertEquals( "alleyinsider.com/abc" , AppContextHolder.fixBase( "www.alleyinsider.com" , "/abc" ).toString() );
        assertEquals( "alleyinsider.com/abc" , AppContextHolder.fixBase( "www.alleyinsider.com" , "abc" ).toString() );

        assertEquals( "alleyinsider.com/abc" , AppContextHolder.fixBase( "alleyinsider.com" , "/abc" ).toString() );
        assertEquals( "alleyinsider.com/abc" , AppContextHolder.fixBase( "alleyinsider.com" , "abc" ).toString() );

        assertEquals( "alleyinsider.com/abc" , AppContextHolder.fixBase( "alleyinsider.10gen.com" , "abc" ).toString() );

        assertEquals( "alleyinsider.com/abc" , AppContextHolder.fixBase( "static.10gen.com" , "/www.alleyinsider.com/abc" ).toString() );
        assertEquals( "alleyinsider.com/abc" , AppContextHolder.fixBase( "static.10gen.com" , "www.alleyinsider.com/abc" ).toString() );
        
        assertEquals( "alleyinsider.com/abc" , AppContextHolder.fixBase( "static.10gen.com" , "/alleyinsider.com/abc" ).toString() );
        assertEquals( "alleyinsider.com/abc" , AppContextHolder.fixBase( "static.10gen.com" , "alleyinsider.com/abc" ).toString() );

        
        assertEquals( "foo.co.uk/abc" , AppContextHolder.fixBase( "foo.co.uk" , "abc" ).toString() );
        assertEquals( "foo.co.uk/abc" , AppContextHolder.fixBase( "foo.co.uk.10gen.com" , "abc" ).toString() );

    }

    @Test(groups = {"basic"})
    public void testGetEnvironmentName(){
        assertEquals( "www" , ( new Info( "www.alleyinsider.com" ) ).getEnvironment( "www.alleyinsider.com" ) );
        assertEquals( "www" , ( new Info( "alleyinsider.com" ) ).getEnvironment( "www.alleyinsider.com" ) );
        assertEquals( "www" , ( new Info( "www.alleyinsider.com" ) ).getEnvironment( "alleyinsider.com" ) );

        assertEquals( "dev" , ( new Info( "alleyinsider.com" ) ).getEnvironment( "dev.alleyinsider.com" ) );

    }


    @Test(groups = {"basic"})
    public void testNotExist(){
        
    }

    public static void main( String args[] ){
        (new AppServerTest()).runConsole();
    }
}
