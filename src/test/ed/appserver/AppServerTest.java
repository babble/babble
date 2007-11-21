// AppServerTest.java

package ed.appserver;

public class AppServerTest extends ed.TestCase {

    public void testGetContext(){
        AppServer as = new AppServer( "/data/sites/admin" , "/data/sites/" );

        assertEquals( "/data/sites/alleyinsider" , as.getContext( "alleyinsider.latenightcoders.com" , "" , null ).getRoot() );
        assertEquals( "/data/sites/alleyinsider" , as.getContext( "alleyinsider.com" , "" , null ).getRoot() );
        
        assertEquals( "/data/sites/admin" , as.getContext( "sb1.latenightcoders.com" , "" , null ).getRoot() );
        assertEquals( "/data/sites/admin" , as.getContext( "latenightcoders.com" , "" , null ).getRoot() );
        assertEquals( "/data/sites/admin" , as.getContext( "" , "" , null ).getRoot() );

        String newUri[] = new String[1];

        assertEquals( "/data/sites/alleyinsider" , as.getContext( "origin.10gen.com" , "/alleyinsider/images/logo.gif" , newUri ).getRoot() );
        assertEquals( "/images/logo.gif" , newUri[0] );
        assertEquals( "/data/sites/admin" , as.getContext( "www.10gen.com" , "/alleyinsider/images/logo.gif" , newUri ).getRoot() );
        assertEquals( null , newUri[0] );
    }

    public static void main( String args[] ){
        (new AppServerTest()).runConsole();
    }
}
