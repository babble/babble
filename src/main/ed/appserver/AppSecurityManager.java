// AppSecurityManager.java

package ed.appserver;

import java.io.*;
import java.util.*;
import java.security.*;

import ed.io.*;
import ed.js.*;
import ed.js.engine.*;
import ed.log.*;
import ed.lang.*;
import ed.util.*;

public final class AppSecurityManager extends SecurityManager {

    static boolean READY = false;
    
    AppSecurityManager(){
        _os = Machine.getOSType();
        _logger = Logger.getLogger( "security" );
        _javaRoot = (new File(".")).getAbsolutePath().replaceAll( "\\.$" , "" );

        List<String> okRead = new ArrayList<String>();
        List<String> okWrite = new ArrayList<String>();

        okRead.add( _javaRoot );
        okRead.add( "include" );
        okRead.add( "build" );
        okRead.add( "conf" );
        okRead.add( "src" );
        okRead.add( "." );
        okRead.add( "/opt/java/" );
        okWrite.add( "logs/" );        

        okRead.add( System.getProperty( "user.home" ) + "/.jython" );
        
        okWrite.add( WorkingFiles.getTypeDir( "jython-cache" ).getAbsolutePath() );

        
        if ( _os.isMac() ){
            okWrite.add( "/private" + WorkingFiles.getTypeDir( "jython-cache" ).getAbsolutePath() );
            okRead.add( "/System/" );
            okRead.add( "/Library/" );
        }
        else if ( _os.isLinux() ){
            okRead.add( "/usr/" );
        }

        System.out.println( "okRead : " + okRead );
        System.out.println( "okWrite : " + okWrite );

        _okRead = new String[okRead.size()];
        okRead.toArray( _okRead );

        _okWrite = new String[okWrite.size()];
        okWrite.toArray( _okWrite );

        _jsCompileRoot = CompileUtil.getCompileSrcDir( Convert.DEFAULT_PACKAGE ).replaceAll( "/+$" , "" );


        _publicClasses = new String[_publicDirs.length];
        for ( int i=0; i<_publicDirs.length; i++ )
            _publicClasses[i] = Convert.cleanName( _publicDirs[i] );
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

        if ( read )
            for ( int i=0; i<_okRead.length; i++ )
                if ( file.startsWith( _okRead[i] ) )
                    return;
        
        for ( int i=0; i<_okWrite.length; i++ )
            if ( file.startsWith( _okWrite[i] ) )
                return;
        
        for ( int i=0; i<_publicDirs.length; i++ )
            if ( file.contains( _publicDirs[i] ) )
                return;
        
        if ( file.startsWith( ctxt.getRoot() ) )
            return;
        
        if ( file.contains( ctxt.getRoot() ) ){
            if ( file.startsWith( _javaRoot + ctxt.getRoot() ) )
                return;
        }
            

        final StackTraceElement topUser = ed.security.Security.getTopUserStackElement();
        
        if ( topUser == null )
            return;

        if ( file.startsWith( _jsCompileRoot ) ){
            String wantSitePiece = file.substring( _jsCompileRoot.length() );
            // this is basically just the dir
            if ( wantSitePiece.length() < 3 )
                return;
            
            while ( wantSitePiece.startsWith( "/" ) )
                wantSitePiece = wantSitePiece.substring(1);
            
            // TODO - modules have to be a lot smarter
            //        this is obviously full of holes
            for ( int i=0; i<_publicClasses.length; i++ )
                if ( wantSitePiece.contains( _publicClasses[i] ) )
                    return;
            
            String goodSitePiece = Convert.cleanName( ctxt.getRoot() );
            if ( wantSitePiece.startsWith( goodSitePiece ) )
                return;

            System.err.println( "blah [" + wantSitePiece + "] [" + goodSitePiece + "]" );
        }
        
        NotAllowed e = new NotAllowed( "not allowed to access [" + file + "] from [" + topUser + "] in site [" + ctxt.getRoot() + "]" + fp , fp );
        e.fillInStackTrace();
        _logger.error( "invalid access [" + fp + "]" , e );
        throw e;
    }
    
    final String[] _okRead;
    final String[] _okWrite;
    final Machine.OSType _os;
    final Logger _logger;
    final String _jsCompileRoot;
    final String _javaRoot;

    final String[] _publicDirs = new String[]{ "/corejs/" , "/external/" , "/core-modules/" , "/site-modules/" , "src/main/ed/" }; // site-modules is special
    final String[] _publicClasses;

    static class NotAllowed extends AccessControlException implements StackTraceHolder.NoFix {
        NotAllowed( String msg , Permission p ){
            super( msg , p );
        }
    }
}
