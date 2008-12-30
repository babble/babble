// ManagerTest.java

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

import java.io.*;
import java.util.*;

import org.testng.annotations.Test;

public class ManagerTest extends ed.TestCase {
    
    class MyApplicationFactory implements ApplicationFactory {
        
        public List<Application> getApplications(){
            List<Application> l = new LinkedList<Application>();
            l.add( new JavaApplication( "test" , "test1" , "ed.manager.TestApp" , -1 , new String[0] , new String[0] , false ) );
            return l;
        }

        public long timeBetweenRefresh(){
            return Long.MAX_VALUE;
        }

        public boolean runGridApplication(){
            return false;
        }

        public String textView(){
            return "debug";
        }

    }

    @Test
    public void testJavaApplicationCommands(){
        assertEquals( JavaApplication.JAVA + " Foo" , 
                      _toString( JavaApplication._getCommands( null , "Foo" , null , null , -1 , false ) ) );

        assertEquals( JavaApplication.JAVA + " -Xmx200m Foo" , 
                      _toString( JavaApplication._getCommands( null , "Foo" , null , null , 200 , false ) ) );

        assertEquals( JavaApplication.JAVA + " -Xmx200m Foo blah" , 
                      _toString( JavaApplication._getCommands( null , "Foo" , new String[]{ "blah" } , null , 200 , false ) ) );

        assertEquals( JavaApplication.JAVA + " -Xmx200m -Xa -Xb Foo blah" , 
                      _toString( JavaApplication._getCommands( null , "Foo" , new String[]{ "blah" } , new String[]{ "-Xa" , "-Xb" } , 200 , false ) ) );

        assertEquals( JavaApplication.JAVA + " -Xmx200m -Xa -Xb Foo blah \"c d\"" , 
                      _toString( JavaApplication._getCommands( null , "Foo" , new String[]{ "blah" , "c d" } , new String[]{ "-Xa" , "-Xb" } , 200 , false ) ) );
                      
    }

    @Test
    public void testRunning()
        throws Exception {
        Manager m = new Manager( new MyApplicationFactory() );
        m.start();
        Thread.sleep( 100 );
        assertEquals( 1 , m.getApplications().size() );
        RunningApplication ra = m.getRunning( m.getApplications().get( 0 ) );
        assertEquals( 1 , ra.timesStarted() );
        m.join();

        assertEquals( "OUT: hello" , ra.outputLine( 2 ) );
        assertEquals( "OUT: goodbye" , ra.outputLine( 1 ) );
        assertEquals( "ERR: done" , ra.outputLine( 0 ) );
    }


    @Test
    public void testPause() 
        throws Exception {
        Manager m = new Manager( new MyApplicationFactory() );
        m.start();
        Thread.sleep( 100 );

        Application app = m.getApplications().get( 0 );
        assertEquals( m.getRunning( app ).timesStarted(), 1 );
        assertEquals( m.getRunning( app ).isDone(), false );

        // pause
        m.togglePause( app );
        Thread.sleep( 100 );
        assertEquals( m.getRunning( app ).isDone(), true );
        assertEquals( m.isPaused( app ), true );

        // unpause
        m.togglePause( app );
        Thread.sleep( 100 );
        assertEquals( m.getRunning( app ).timesStarted(), 1 );
        assertEquals( m.getRunning( app ).isDone(), false );
        assertEquals( m.isPaused( app ), false );

        m.join();
    }
    
    String _toString( String[] s ){
        StringBuilder buf = new StringBuilder();
        for ( int i=0; i<s.length; i++ ){
            if ( i > 0 )
                buf.append( " " );
            buf.append( s[i] );
        }
        return buf.toString();
    }

    public static void main( String args[] ){
        (new ManagerTest()).runConsole();
    }
}
