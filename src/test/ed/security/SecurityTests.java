// SecurityTests.java

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

/**
 * this needs to run in its own jvm
 * as it messed with a bunch of stuff
 */

package ed.security;

import java.io.*;
import java.net.*;
import java.security.*;

import ed.*;
import ed.db.*;
import ed.appserver.*;
import ed.security.*;

public class SecurityTests extends TestCase {

    public SecurityTests(){
        _context = new AppContext( new File( "src/test/samplewww" ) );            
        
        System.setSecurityManager( new AppSecurityManager() );
        AppSecurityManager.ready();

        Security.addDynamicClass( "ed.security.SecurityTests" );
    }


    public void testJavaClassNames(){
        assertTrue( Security.nonSecureCanAccessClass( "java.util.List" ) );
        assertTrue( Security.nonSecureCanAccessClass( "ed.js.JSMath" ) );

        assertFalse( Security.nonSecureCanAccessClass( "ed.js.engine.Scope" ) );
        assertFalse( Security.nonSecureCanAccessClass( "ed.db.Mongo" ) );
    }
    
    public void testListenAllowed()
        throws IOException {
        AppContext.clearThreadLocal();
        ServerSocket ss = new ServerSocket( 44444 );
        ss.close();
    }
    
    public void testListenNotAllowed()
        throws IOException {
        _context.makeThreadLocal();
        
        try {
            ServerSocket ss = new ServerSocket( 44444 );
            assert( false );
        }
        catch ( AccessControlException ace ){
        }
        
    }

    public void testSocketOpenGood()
        throws IOException {
        _context.makeThreadLocal();
        Socket s = new Socket( "127.0.0.1" , 22 );
        s.close();
        
    }

    public void testSocketOpenBad()
        throws IOException {
        _context.makeThreadLocal();
        try {
            Socket s = new Socket( "127.0.0.1" , 27017 );
            assert( false );
        }
        catch ( AccessControlException ace ){
        }
        
    }

    public void testDBOpen()
        throws IOException {
        _context.makeThreadLocal();
        
        DBPort p = new DBPort( new InetSocketAddress( "127.0.0.1" , 27017 ) );
        p.ensureOpen();
    }
    
    /**
     * this test doesn't work yet
     * SecurityManager doesn't seem to be able to control creating a thread
     */
    public void _testThreadOpen(){
        _context.makeThreadLocal();
        Thread t = new Thread(){
                public void run(){
                    throw new RuntimeException( "shouldn't get ehre" );
                }
            };
        
        try {
            t.start();
            //assert( false );
        }
        catch ( AccessControlException ace ){
        }
    }
    
    public void testExec()
        throws IOException {
        try {
            Runtime.getRuntime().exec( "ls" );
            assert( false );
        }
        catch ( AccessControlException ace ){}
    }
    
    final AppContext _context;

    public static void main( String args[] ){
        (new SecurityTests()).runConsole();
    }
    
}
