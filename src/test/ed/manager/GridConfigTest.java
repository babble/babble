// GridConfigTest.java

package ed.manager;

import org.testng.annotations.Test;

import java.util.*;

import ed.*;
import ed.js.*;
import ed.cloud.*;

public class GridConfigTest extends TestCase {
    
    class MyGridConfig extends GridConfigApplicationFactory {
        MyGridConfig( Map<String,JSObject[]> types ){
            super( Cloud.getInstance() );
            _types = types;
        }

        protected Iterable<JSObject> _find( String type ){
            JSObject[] arr = _types.get( type );
            if ( arr == null )
                return new LinkedList<JSObject>();
            
            return Arrays.asList( arr );
        }
        
        final Map<String,JSObject[]> _types;
    }
        

    public void test1()
        throws Exception {
        
        Map<String,JSObject[]> m = new TreeMap<String,JSObject[]>();
        m.put( "dbs" , new JSObject[]{ 
                JS.build( new String[]{ "name" , "machine" } , new Object[]{ "t1" , _cloud.getServerName() } )
            } );
        
        MyGridConfig factory = new MyGridConfig( m );
        assertClose( "db t1 ACTIVE : true master : true " , factory.getConfig() );
    }

    public void test2()
        throws Exception {
        
        Map<String,JSObject[]> m = new TreeMap<String,JSObject[]>();
        m.put( "dbs" , new JSObject[]{ 
                JS.build( new String[]{ "name" , "pairs" } , 
                          new Object[]{ "t2" , Arrays.asList( new String[]{ _cloud.getServerName() , "foo.com" } ) }
                          )
            } );
        
        MyGridConfig factory = new MyGridConfig( m );
        assertClose( "db t2 ACTIVE : true pairwith : foo.com" , factory.getConfig() );
    }

    final Cloud _cloud = Cloud.getInstance();
        
    public static void main( String args[] ){
        (new GridConfigTest()).runConsole();
    }
}
