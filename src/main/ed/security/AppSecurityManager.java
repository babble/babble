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
        if ( ! READY )
            return;
        
        if ( perm instanceof FilePermission )
            checkFilePermission( (FilePermission)perm );
        
    }

    public void checkPermission(Permission perm, Object context){}

    
    final void checkFilePermission( FilePermission fp ){
        final AppRequest ar = AppRequest.getThreadLocal();
        if ( ar == null )
            return;
        
        final AppContext ctxt = ar.getContext();
        
        final String file = fp.getName();
        final String action = fp.getActions();
        final boolean read = action.equals( "read" );
       
        if ( _file.allowed( ctxt , file , read ) )
            return;
       
        final StackTraceElement topUser = ed.security.Security.getTopUserStackElement();
              
        NotAllowed e = new NotAllowed( "not allowed to access [" + file + "] from [" + topUser + "] in site [" + ctxt + "]" + fp , fp );
        e.fillInStackTrace();
        _logger.error( "invalid access [" + fp + "]" , e );
        throw e;
    }
    
    final Logger _logger;
    final FileSecurity _file;

    static class NotAllowed extends AccessControlException implements StackTraceHolder.NoFix {
        NotAllowed( String msg , Permission p ){
            super( msg , p );
        }
    }
}
