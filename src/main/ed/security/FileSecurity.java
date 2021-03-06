// FileSecurity.java


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
import java.util.*;
import java.util.regex.*;
import java.security.*;

import ed.io.*;
import ed.js.*;
import ed.js.engine.*;
import ed.log.*;
import ed.lang.*;
import ed.util.*;
import ed.appserver.*;

class FileSecurity {

    private static FileSecurity INSTANCE;

    static synchronized FileSecurity getInstance(){
        if ( INSTANCE == null )
            INSTANCE = new FileSecurity();
        return INSTANCE;
    }
    
    static FileSecurity getInstanceIfCreated(){
        return INSTANCE;
    }

    private FileSecurity(){
        _os = Machine.getOSType();
        _logger = Logger.getLogger( "security.filesystem" );
        _javaRoot = (new File(".")).getAbsolutePath().replaceAll( "\\.$" , "" );

        List<String> okRead = new UniqueList<String>();
        List<String> okWrite = new UniqueList<String>();
        
        
        okRead.add( _javaRoot );
        
        addLocal( okRead , okWrite , _javaRoot );
        addLocal( okRead , okWrite , "" );

        okRead.add( "/opt/java/" );
        
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
        
        {   // add the java home read only
            final String javaHome = System.getProperty( "java.home" );
            int idx = javaHome.lastIndexOf( File.separator );
            okRead.add( javaHome.substring( 0 , idx ) );
        }
        
        // finalize things

        _logger.info( "okRead : " + okRead );
        _logger.info( "okWrite : " + okWrite );
        
        _okRead = new String[okRead.size()];
        okRead.toArray( _okRead );
        
        _okWrite = new String[okWrite.size()];
        okWrite.toArray( _okWrite );

        _jsCompileRoot = CompileUtil.getCompileSrcDir( Convert.DEFAULT_PACKAGE ).replaceAll( "/+$" , "" );

        _publicClasses = new String[_publicDirs.length];
        for ( int i=0; i<_publicDirs.length; i++ )
            _publicClasses[i] = Convert.cleanName( _publicDirs[i] );
    }

    private void addLocal( List<String> okRead , List<String> okWrite , String root ){
        okRead.add( root + "include" );
        okRead.add( root + "build" );
        okRead.add( root + "conf" );
        okRead.add( root + "src" );
        okRead.add( root + "." );
        okWrite.add( root + "logs/" );        
    }

    final boolean canRead( AppContext context , File f ){
        if ( context == null )
            return true;

        if ( context.getRootFile().getAbsolutePath().startsWith( f.getAbsolutePath() ) )
            return true;

        return canRead( context , f.getAbsolutePath() );
    }

    final boolean canRead( String file ){
        return allowed( null , file , true );
    }

    final boolean canWrite( String file ){
        return allowed( null , file , false );
    }

    final boolean canRead( AppContext ctxt , String file ){
        return allowed( ctxt , file , true );
    }

    final boolean canWrite( AppContext ctxt , String file ){
        return allowed( ctxt , file , false );
    }

    
    final boolean allowed( AppContext ctxt , String file , boolean read ){
        if ( ctxt == null ){
            ctxt = AppSecurityManager._appContext();
            if ( ctxt == null )
                return true;
        }
        
        if ( read )
            for ( int i=0; i<_okRead.length; i++ )
                if ( _issub( _okRead[i] , file ) )
                    return true;
        
        for ( int i=0; i<_okWrite.length; i++ )
            if ( _issub( _okWrite[i] , file ) )
                return true;
        
        for ( int i=0; i<_publicDirs.length; i++ )
            if ( file.contains( _publicDirs[i] ) )
                return true;
        
        if ( _checkInContext( ctxt.getRoot() , file ) || 
             _checkInContext( ctxt.getRootFile().getAbsolutePath() , file ) )
            return true;
        
        if ( file.startsWith( _jsCompileRoot ) ){
            String wantSitePiece = file.substring( _jsCompileRoot.length() );
            // this is basically just the dir
            if ( wantSitePiece.length() < 3 )
                return true;
            
            while ( wantSitePiece.startsWith( "/" ) )
                wantSitePiece = wantSitePiece.substring(1);
            
            // TODO - modules have to be a lot smarter
            //        this is obviously full of holes
            for ( int i=0; i<_publicClasses.length; i++ )
                if ( wantSitePiece.contains( _publicClasses[i] ) )
                    return true;
            
            String goodSitePiece = Convert.cleanName( ctxt.getRoot() );
            if ( wantSitePiece.startsWith( goodSitePiece ) )
                return true;

            System.err.println( "blah [" + wantSitePiece + "] [" + goodSitePiece + "]" );
        }
        
        return false;
    }

    boolean _checkInContext( final String rawContextRoot , final String file ){
        final String ctxtRoot;
        {
            String root = _cleanBack( rawContextRoot );
            if ( ! root.endsWith( "/" ) ){
                // this is so you can't have one site with ab and one with a and have a hole
                root += "/";
            }
            ctxtRoot = root;
        }

        if ( _issub( ctxtRoot , file ) )
            return true;

        if ( file.contains( ctxtRoot ) ){
            if ( file.startsWith( _javaRoot + ctxtRoot ) )
                return true;
        }

        return false;
    }

    static boolean _issub( String good , String file ){
        if ( file.startsWith( good ) )
            return true;
        
        if ( good.length() == file.length() + 1 &&
             good.startsWith( file ) &&
             good.charAt( good.length() -1 ) == File.separatorChar )
            return true;

        return false;
    }

    static String _cleanBack( String file ){

        while ( true ){
            int before = file.length();
            file = _subdirPattern.matcher( file ).replaceAll( "/" );
            if ( file.length() == before )
                return file;
        }
    }
    
    final String[] _okRead;
    final String[] _okWrite;
    final Machine.OSType _os;
    final Logger _logger;
    final String _jsCompileRoot;
    final String _javaRoot;

    final String[] _publicDirs = new String[]{ "/corejs/" , "/external/" , "/core-modules/" , "/site-modules/" , "src/main/ed/" }; // site-modules is special
    final String[] _publicClasses;   

    final static Pattern _subdirPattern = Pattern.compile( "/[^/]+/\\.\\./" );
}
