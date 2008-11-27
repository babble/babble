// AppSecurityManager.java

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

package ed.security;

import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;

import ed.io.*;
import ed.js.*;
import ed.js.engine.*;
import ed.log.*;
import ed.lang.*;
import ed.util.*;
import ed.appserver.*;

public final class AppSecurityManager extends SecurityManager {

    private static boolean READY = false;
    
    public static void ready(){
        READY = true;
    }

    public AppSecurityManager(){
        _logger = Logger.getLogger( "security" );
        _file = new FileSecurity();
    }
    
    public void checkPermission(Permission perm) {
        if ( ! READY || perm == null )
            return;

        final AppContext context = AppContext.findThreadLocal();
        if ( context == null ){
            // this means we have to be in core server code
            // TODO: make sure there is no way for a user to unset
            return;
        }

        if ( Security.inTrustedCode() )
            return;
        
        if ( perm instanceof FilePermission ){
            checkFilePermission( context , (FilePermission)perm );
        }
        else if ( perm instanceof SocketPermission ){
            checkSocketPermission( (SocketPermission)perm );
        }
        else if ( perm instanceof javax.security.auth.kerberos.ServicePermission || 
                  perm instanceof javax.security.auth.PrivateCredentialPermission ){
            // these are things that are kind of irrelevant for our security
            // they're totally safe as far as i can tell
            // so just allowing them all for completeness
        }
        else {
            if ( ! _seenPermissions.contains( perm.getClass() ) ){
                _seenPermissions.add( perm.getClass() );
                _logger.getChild( "unknown-perm" ).info( perm.getClass().getName() + " [" + perm + "] from [" + Security.getTopDynamicClassName() + "]" );
            }
        }
    }
    
    public void checkPermission(Permission perm, Object context){}

    final void checkSocketPermission( SocketPermission perm ){
        
        final String action = perm.getActions();
        if ( action.contains( "listen" ) )
            throw new NotAllowed( "can't listen to a socket : " + perm , perm );


        if ( action.contains( "connect" ) ){
            final String name = perm.getName();
            final int idx = name.indexOf( ":" );
            if ( idx < 0 )
                throw new RuntimeException( "got a name of a SocketPermission that i don't get [" + name + "]" );
            
            final String host = name.substring( 0 , idx );
            final int port = Integer.parseInt( name.substring( idx + 1 ) );
            
            checkConnect( host , port , perm );
            return;
        }
        
    }

    final void checkConnect( String host , int port , SocketPermission perm ){

        // all connections are safe except db for now

        if ( port < 27000 || port > 28000 ){
            // db port and range around it
            return;
        }

        // db connetions have to come from ed.db
        
        StackTraceElement calling = getCallingElement();
        if ( calling.getClassName().startsWith( "ed.db" ) )
            return;

        throw new NotAllowed( "can't open connection to " + host + ":" + port + " from : " + calling , perm );
    }
    
    final void checkFilePermission( AppContext context , FilePermission fp ){
        
        final String action = fp.getActions();

        if ( action.contains( "execute" ) ){
            throw new NotAllowed( "not allowed to exec" , fp );
        }

        final String file = fp.getName();
        final boolean read = action.equals( "read" );
        
        if ( _file.allowed( context , file , read ) )
            return;
       
        throw new NotAllowed( "not allowed to access [" + file + "] from [" + Security.getTopDynamicStackFrame() + "] in site [" + context + "]" + fp , fp );
    }

    StackTraceElement getCallingElement(){
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        
        for ( int i=0; i<st.length; i++ ){
            StackTraceElement e = st[i];
            
            final String name = e.getClassName();
            if ( name.startsWith( "java.lang." ) || 
                 name.startsWith( "sun." ) )
                continue;

            if ( name.equals( "ed.security.AppSecurityManager" ) )
                continue;

            
            return e;
        }

        return null;
    }

    // --- lower level ---

    public void checkExec( String cmd ){
        if ( AppContext.findThreadLocal() != null && ! Security.inTrustedCode() )
            throw new NotAllowed( "can't exec [" + cmd + "]" , new FilePermission( cmd , "execute" ) );
    }


    // -------------------
    
    final Logger _logger;
    final FileSecurity _file;

    final Set<Class> _seenPermissions = new HashSet<Class>();

    static class NotAllowed extends AccessControlException implements StackTraceHolder.NoFix {
        NotAllowed( String msg , Permission p ){
            super( msg , p );
        }
    }
}
