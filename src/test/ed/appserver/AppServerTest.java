// AppServerTest.java

package ed.appserver;

import org.testng.annotations.Test;

public class AppServerTest extends ed.TestCase {

    String _root = "src/test/data/";

    @Test(groups = {"basic"})
    public void testGetContext(){
        AppContextHolder as = new AppContextHolder( _root + "admin" , _root );
        
        assertEquals( _root + "alleyinsider" , as.getContext( "alleyinsider.latenightcoders.com" , "" , null ).getRoot() );
        assertEquals( _root + "alleyinsider" , as.getContext( "alleyinsider.com" , "" , null ).getRoot() );
        
        assertEquals( _root + "admin" , as.getContext( "sb1.latenightcoders.com" , "" , null ).getRoot() );
        assertEquals( _root + "admin" , as.getContext( "latenightcoders.com" , "" , null ).getRoot() );
        assertEquals( _root + "admin" , as.getContext( "" , "" , null ).getRoot() );
        
        assertEquals( _root + "a/www" , as.getContext( "a.com" , "" , null ).getRoot() );
        assertEquals( _root + "a/www" , as.getContext( "www.a.com" , "" , null ).getRoot() );
        assertEquals( _root + "a/dev" , as.getContext( "dev.a.com" , "" , null ).getRoot() );
        
        String newUri[] = new String[1];
        
        assertEquals( _root + "alleyinsider" , as.getContext( "origin.10gen.com" , "/alleyinsider/images/logo.gif" , newUri ).getRoot() );
        assertEquals( "/images/logo.gif" , newUri[0] );
        assertEquals( _root + "admin" , as.getContext( "www.10gen.com" , "/alleyinsider/images/logo.gif" , newUri ).getRoot() );
        assertEquals( null , newUri[0] );
    }

    public static void main( String args[] ){
        (new AppServerTest()).runConsole();
    }
}
