// RunningApplicationTest.java

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

public class RunningApplicationTest extends ed.TestCase {
    
    class MyApplicationFactory implements ApplicationFactory {
        
        public List<Application> getApplications(){
            List<Application> l = new LinkedList<Application>();
            l.add( new JavaApplication( "test" , "test1" , "ed.manager.TestApp" , -1 , new String[0] , new String[0] , false ) );
            l.add( new JavaApplication( "test" , "test1" , "ed.manager.TestApp2" , -1 , new String[0] , new String[0] , false ) );
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
    public void testRunning()
        throws Exception {
        Manager m = new Manager( new MyApplicationFactory() );
        m.start();
        Thread.sleep( 100 );
        RunningApplication ra = m.getRunning( m.getApplications().get( 0 ) );
        m.shutdown();
        System.out.println( ra.isDone() );
        m.join();
    }
    
    @Test
    public void testQuick() 
        throws Exception {

        Manager m = new Manager( new MyApplicationFactory() );
        m.start();
        Thread.sleep( 100 );
        RunningApplication ra = m.getRunning( m.getApplications().get( 0 ) );
        m.shutdown();
        System.out.println( ra.isDone() );
        m.join();
    }
    
    public static void main( String args[] ){
        (new RunningApplicationTest()).runConsole();
    }
}
