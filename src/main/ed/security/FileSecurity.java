// FileSecurity.java


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

class FileSecurity {

    public FileSecurity(){
        _os = Machine.getOSType();
        _logger = Logger.getLogger( "security.filesystem" );
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

    final boolean allowed( AppContext ctxt , String file , boolean read ){
        final String ctxtRoot = ctxt.getRoot();
        
        if ( read )
            for ( int i=0; i<_okRead.length; i++ )
                if ( file.startsWith( _okRead[i] ) )
                    return true;
        
        for ( int i=0; i<_okWrite.length; i++ )
            if ( file.startsWith( _okWrite[i] ) )
                return true;
        
        for ( int i=0; i<_publicDirs.length; i++ )
            if ( file.contains( _publicDirs[i] ) )
                return true;
        
        if ( file.startsWith( ctxtRoot ) )
            return true;
        
        if ( ctxtRoot.startsWith( file ) && 
             ctxtRoot.length() - file.length() <= 1 && 
             ctxtRoot.endsWith( "/" ) )
            return true;

        if ( file.contains( ctxtRoot ) ){
            if ( file.startsWith( _javaRoot + ctxtRoot ) )
                return true;
        }
        
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
            
            String goodSitePiece = Convert.cleanName( ctxtRoot );
            if ( wantSitePiece.startsWith( goodSitePiece ) )
                return true;

            System.err.println( "blah [" + wantSitePiece + "] [" + goodSitePiece + "]" );
        }
        
        return false;
    }
    
    final String[] _okRead;
    final String[] _okWrite;
    final Machine.OSType _os;
    final Logger _logger;
    final String _jsCompileRoot;
    final String _javaRoot;

    final String[] _publicDirs = new String[]{ "/corejs/" , "/external/" , "/core-modules/" , "/site-modules/" , "src/main/ed/" }; // site-modules is special
    final String[] _publicClasses;   
}
