// GitUtils.java

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

package ed.util;

import java.io.*;
import java.util.*;

import ed.io.*;
import ed.js.*;
import ed.log.*;

/** @expose */
public class GitUtils {

    /** @unexpose */
    static final Logger _log = Logger.getLogger( "git" );

    /** Determines if a given directory is a git repository.
     * @param dir The directory to check
     * @return If the directory is a git directory
     */
    public static boolean isSourceDirectory( File dir ){
        if ( hasGit( dir ) )
            return true;

        if (!dir.isDirectory()) {
            return false;
        }

        for ( File t : dir.listFiles() )
            if ( t.getName().endsWith( ".js" ) )
                return true;

        return false;
    }

    /** Checks if a given directory contains a .git file
     * @param dir Directory to check
     * @return If the directory contains a .git file
     */
    public static boolean hasGit( File dir ){
        return (new File( dir , ".git" )).exists();
    }

    /** Get a branch or tag name of a given git repository path.
     * @param dir Path of the git repository
     * @return The branch or tag name
     * @throws RuntimeException If the .git file is not formatted correctly
     */
    public static String getBranchOrTagName( File dir ){

        if ( ! dir.toString().endsWith( ".git" ) )
            dir = new File( dir , ".git" );

        if ( ! dir.exists() )
            throw new RuntimeException( dir + " does not exist" );

        try {
            return _getBranchOrTagName( dir );
        }
        catch ( IOException ioe ){
            // should never happen
            throw new RuntimeException( ioe );
        }
    }

    private static String _getBranchOrTagName( final File git )
        throws IOException {
        String head = StreamUtil.readFully( new File( git , "HEAD" ) ).trim();
        if ( head.startsWith( "ref: refs/heads/" ) )
            return head.substring( 16 ).trim();

        if ( head.length() == 40 ){
            String tag = _findTag( git , head );
            if ( tag != null )
                return tag;

            return head;
        }

        throw new RuntimeException( "dont know what to do with HEAD [" + head + "]" );
    }

    private static String _findTag( final File git , final String hash )
        throws IOException {
        for ( File t : (new File( git , "refs/tags/" ) ).listFiles() ){
            String tag = StreamUtil.readFully( t ).trim();
            if ( tag.equals( hash ) )
                return t.getName();

            SysExec.Result r = SysExec.exec( "git log " + t.getName() + " -n1" , null , git , null );

            if ( r.getOut().startsWith( "commit " ) ){
                int idx = r.getOut().indexOf( "\n" );
                final String newHash = r.getOut().substring( 7 , idx ).trim();
                if ( newHash.equals( hash ) )
                    return t.getName();
            }

        }
        return null;
    }

    /** Downloads a copy of an existing repository to the path specified.
     * @param cloneurl URL of respository to copy
     * @param dirToCloneInto Where to save the cloned repository
     * @param name Optional directory name
     * @return if the clone was successful
     */
    public static boolean clone( String cloneurl , File dirToCloneInto , String name ){
        _log.info( "cloning " + cloneurl + " to " + dirToCloneInto );
        SysExec.Result r = SysExec.exec( "git clone " + cloneurl + " " + ( name == null ? "" : name  ) , null , dirToCloneInto , null );

        if ( r.exitValue() == 0 )
            return true;

        _log.error( "error cloning " + cloneurl + " to " + dirToCloneInto + " " + r );
        return false;
    }

    /** Checkout a given file.
     * @param dir Git directory
     * @param pathspec Path to the file to be checked out from <tt>dir</tt>
     * @return if the checkout was successful
     */
    public static boolean checkout( File dir , String pathspec ){
        _log.info( "checkout " + dir + " to " + pathspec );
        SysExec.Result r = SysExec.exec( "git checkout " + pathspec , null , dir , null );

        if ( r.exitValue() == 0 )
            return true;

        _log.error( "error checking out " + dir + " to " + pathspec + " " + r );
        return false;
    }

    public static boolean onBranch( File dir ){
        SysExec.Result r = SysExec.exec( "git branch" , null , dir , null );
        if ( r.exitValue() != 0 )
            return false;
        
        if ( r.getOut().indexOf( "master" ) < 0 )
            throw new RuntimeException( "something is wrong output [" + r.getOut() + "]" );
        
        return r.getOut().indexOf( "(no branch)" ) < 0;
    }

    /** Tries a "git pull" on the given directory.
     * @param dir The directory on which to pull
     * @return if the pull was successful
     */
    public static boolean pull( File dir ){
        return pull( dir , true );
    }

    /** Tries a "git pull" on the given directory.
     * @param dir The directory on which to pull
     * @param careAboutError if errors should be logged
     * @return if the pull was successful
     */
    public static boolean pull( File dir , boolean careAboutError ){
        _log.info( "pull " + dir );
        SysExec.Result r = SysExec.exec( "git pull" , null , dir , null );

        if ( r.exitValue() == 0 )
            return true;

        if ( careAboutError )
            _log.info( "error pull " + dir + " " + r );
        return false;
    }

    /** Updates all of the remote tracking branches.
     * @param dir The directory to fetch
     * @retirm if the fetch was successful
     */
    public static boolean fetch( File dir ){
        return fetch( dir , "" );
    }
    
    public static boolean fetch( File dir , String options ){
        _log.info( "fetch " + dir );
        SysExec.Result r = SysExec.exec( "git fetch " + ( options == null ? "" : options ) , null , dir , null );

        if ( r.exitValue() == 0 )
            return true;

        _log.info( "error fetch " + dir + " " + r );

        return false;
    }

    public static boolean fullUpdate( File dir ){
        boolean ok = true;
        ok = ok && fetch( dir , "" );
        ok = ok && fetch( dir , " --tags " );
        ok = ok && pull( dir );
        return ok;
    }

    public static Collection<String> getAllBranchAndTagNames( File dir ){
        Set<String> all = new HashSet<String>();
        _getHeads( all , new File( dir , ".git/refs/heads" ) );
        _getHeads( all , new File( dir , ".git/refs/tags" ) );
        _getHeads( all , new File( dir , ".git/refs/remotes/origin" ) );
        all.remove( "HEAD" );
        return new JSArray( all );
    }

    private static void _getHeads( Collection<String> all , File dir ){
        if ( ! dir.exists() )
            return;
        
     	for ( File head : dir.listFiles() )
            all.add( head.getName() );
    }

    public static JSObject readConfig( File dir )
        throws IOException {
        if ( ! dir.exists() )
            throw new FileNotFoundException( dir.toString() );
        
        if ( ! dir.isDirectory() )
            return _readConfig( dir );
        
        File t = new File( dir , ".git/config" );
        if ( t.exists() )
            return _readConfig( t );

        t = new File( dir , "config" );
        if ( t.exists() )
            return _readConfig( t );

        throw new FileNotFoundException( ".git/config" );
    }

    private static JSObject _readConfig( File f )
        throws IOException {
        
        JSObject config = new JSObjectBase();
        
        JSObject cur = null;
        
        LineReader in = new LineReader( f );
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

    /** @unexpose */
    public static void main( String args[] )
        throws Exception {
        System.out.println( getBranchOrTagName( new File( args[0] ) ) );
    }

}
