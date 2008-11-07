// GitDir.java

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

package ed.git;

import java.io.*;
import java.util.*;

import ed.io.*;
import ed.js.*;
import ed.log.*;
import ed.util.*;

public class GitDir {
    
    public GitDir( File f ){
        _root = f;
        _dotGit = new File( _root , ".git" );
        _logger = _rootLog.getChild( _root.getAbsolutePath().replaceAll( "[\\/]" , "_" ) );
    }

    public boolean isValid(){
        return _dotGit.exists() && _dotGit.isDirectory();
    }

    public String getBranchOrTagName()
        throws IOException {
        _assertValid();
        
        String head = StreamUtil.readFully( new File( _dotGit , "HEAD" ) ).trim();
        if ( head.startsWith( "ref: refs/heads/" ) )
            return head.substring( 16 ).trim();
        
        if ( head.length() == 40 ){
            String tag = findTagForHash( head );
            if ( tag != null )
                return tag;
            return head;
        }
        
        throw new RuntimeException( "dont know what to do with HEAD [" + head + "]" );
    }
    
    public String findTagForHash( final String hash )
        throws IOException {
        for ( File t : (new File( _dotGit , "refs/tags/" ) ).listFiles() ){
            
            String tag = StreamUtil.readFully( t ).trim();
            if ( tag.equals( hash ) )
                return t.getName();
            
            SysExec.Result r = _exec( "git log " + t.getName() + " -n1" );
            
            if ( r.getOut().startsWith( "commit " ) ){
                int idx = r.getOut().indexOf( "\n" );
                final String newHash = r.getOut().substring( 7 , idx ).trim();
                if ( newHash.equals( hash ) )
                    return t.getName();
            }
            
        }
        return null;
    }
    
    public boolean checkout( String pathspec ){
        _logger.info( "checkout " + pathspec );
        SysExec.Result r = _exec( "git checkout " + pathspec );

        if ( r.exitValue() == 0 )
            return true;
        
        _logger.error( "error checking out [" + pathspec + "] " + r );
        return false;
    }

    public JSObject readConfig()
        throws IOException {
        
        JSObject config = new JSObjectBase();
        
        JSObject cur = null;
        
        LineReader in = new LineReader( new File( _dotGit , "config" ) );
        for ( String line : in ){
            line = line.trim();
            if ( line.startsWith( "[" ) && line.endsWith( "]" ) ){

                line = line.substring(1);
                line = line.substring( 0 , line.length() - 1 ).trim();
                
                cur = config;

                for ( String foo : line.split( "[\\s\"]+" ) ){
                    foo = foo.trim();
                    if ( foo.length() == 0 )
                        continue;
                    
                    
                    JSObject o = new JSObjectBase();
                    cur.set( foo , o );
                    cur = o;
                }

                continue;
            }
                
            if ( cur == null )
                throw new RuntimeException( "invalid config" );
            
            int idx = line.indexOf( "=" );
            if ( idx < 0 )
                throw new RuntimeException( "invalid config" );
            
            String n = line.substring( 0 , idx ).trim();
            String v = line.substring( idx + 1 ).trim();
            cur.set( n , v );
        }
        
        return config;
    }



    public Collection<String> getAllBranchAndTagNames(){
        Collection<String> all = new ArrayList<String>();
        _getHeads( all , new File( _dotGit , "refs/heads" ) );
        _getHeads( all , new File( _dotGit , "refs/tags" ) );
        _getHeads( all , new File( _dotGit , "refs/remotes/origin" ) );
        all.remove( "HEAD" );
        return new JSArray( all );
    }

    private void _getHeads( Collection<String> all , File dir ){
        if ( ! dir.exists() )
            return;
        
     	for ( File head : dir.listFiles() ){
            String name = head.getName();
            if ( all.contains( name ) )
                continue;
            all.add( name );
        }
    }
    

    private void _assertValid(){
        if ( ! isValid() )
            throw new RuntimeException( "directory [" + _root + "] is not a valid git directory" );
    }

    SysExec.Result _exec( String cmd ){
        return _exec( cmd , null , null );
    }

    SysExec.Result _exec( String cmd , String[] env , String toSend ){
        return SysExec.exec( cmd , env , _root , toSend );
    }

    final File _root;
    final File _dotGit;
    final Logger _logger;

    static final Logger _rootLog = Logger.getLogger( "git" );
}
