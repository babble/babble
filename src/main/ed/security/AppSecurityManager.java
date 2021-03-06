// AppSecurityManager.java

/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
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
        _file = FileSecurity.getInstance();
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
        
        if ( perm instanceof FilePermission ){
            checkFilePermission( context , (FilePermission)perm );
            return;
        }

        if ( Security.inTrustedCode() )
            return;

        if ( perm instanceof SocketPermission ){
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

        if ( port < 27000 || port > 29000 ){
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
            checkExec( fp.getName() );
            return;
        }
        
        final String file = fp.getName();
        final boolean read = action.equals( "read" );
        
        if ( _file.allowed( context , file , read ) )
            return;
        
        if ( Security.inTrustedCode() )
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
        if ( notReady() ) return;
        rejectIfNotTrusted( "can't exec [" + cmd + "]" );
    }

    public void checkExit( int status ){
        if ( notReady() ) return;
        rejectIfNotTrusted( "can't exit the JVM"  );
    }
    
    public void checkPrintJobAccess(){
        if ( notReady() ) return;
        rejectIfNotTrusted( "you can't print silly" );
    }

    public void checkSystemClipboardAccess(){
        if ( notReady() ) return;
        rejectIfNotTrusted( "can't use system clipboard" );
    }

    public void checkAccept(String host, int port){
        if ( notReady() ) return;
        rejectIfNotTrusted( "acn't access " + host + ":" + port );
    }
    
    public void checkConnect( String host , int port ){
        if ( notReady() ) return;
        SocketPermission sp = null;
        checkConnect( host , port , sp );
    }

    public void checkConnect( String host , int port , Object context ){
        if ( notReady() ) return;
        SocketPermission sp = null;
        checkConnect( host , port , sp );
    }
    
    public void checkRead(String file){
        if ( notReady() ) return;
        AppContext context = AppContext.findThreadLocal();
        if ( context == null )
            return;
        
        if ( _file.allowed( context , file , true ) )
            return;
        
        if ( Security.inTrustedCode() )
            return;

        throw new NotAllowed( "can't read [" + file + "] from context [" + context.getRoot() + "]" );
    }
    
    public void checkWrite(String file){
        if ( notReady() ) return;

        AppContext context = _appContext();
        if ( context == null )
            return;
        
        if ( _file.allowed( context , file , false ) )
            return;

        throw new NotAllowed( "can't write [" + file + "]" );
    }
    
    void rejectIfNotTrusted( String msg ){
        if ( notReady() ) return;

        if ( AppContext.findThreadLocal() == null || 
             Security.inTrustedCode() )        
            return;
        throw new NotAllowed( msg + " from [" + Security.getTopDynamicClassName() + "]" , null );
    }

    static AppContext _appContext(){
        if ( Security.inTrustedCode() )
            return null;
        return AppContext.findThreadLocal();
    }

    final boolean notReady(){
        return ! READY;
    }
    
    // -------------------
    
    final Logger _logger;
    final FileSecurity _file;

    final Set<Class> _seenPermissions = new HashSet<Class>();
    
    static class NotAllowed extends AccessControlException implements StackTraceHolder.NoFix {

        NotAllowed( String msg ){
            super( msg , null );
        }
        
        NotAllowed( String msg , Permission p ){
            super( msg , p );
        }
    }
}
