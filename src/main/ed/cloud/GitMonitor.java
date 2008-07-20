// GitMonitor.java

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

package ed.cloud;

import java.io.*;
import java.util.*;

import net.contentobjects.jnotify.*;

import ed.io.*;
import ed.log.*;

public class GitMonitor {

    static boolean D = Boolean.getBoolean( "DEBUG.GIT" );
    static Logger _log = Logger.getLogger( "cloud.git.monitor" );

    public GitMonitor(){
	this( "/data/gitroot/" );
    }

    public GitMonitor( String gitroot ){
	_gitroot = new File( gitroot );
	if ( ! _gitroot.exists() )
	    throw new RuntimeException( gitroot + "doesn't exist" );

	_sites = new File( _gitroot , "sites" );
	if ( ! _sites.exists() )
	    throw new RuntimeException( _sites + " doesn't exist" );


	fullCheck();

        _fullChecker = new Thread( "GitMonitor-FullChecker" ){
                public void run(){
                    while ( true ){

                        try {
                            Thread.sleep( 1000 * 60 );
                        }
                        catch ( Exception e ){
                            break;
                        }
                        
                        try {
                            fullCheck();
                        }
                        catch ( Throwable t ){
			    System.err.println( "full check failed" );
                            t.printStackTrace();
                        }
                        
                    }
                }
            };
        
        _fullChecker.setDaemon( true );
        _fullChecker.start();

    }

    
    private void fullCheck(){

	for ( File root : findAllRepositories() ){
	    if ( _watchedDirs.contains( root ) )
		continue;
	    
	    try {
		if ( D ) System.out.println( "repos : " + root );
		
		File heads = new File( root , ".git/refs/heads" );
		_watch( root , heads );
		_checkHeads( heads );

		File tags = new File( root , ".git/refs/tags" );
                if ( tags.exists() ){
                    _watch( root , tags );
                    _checkHeads( tags );
                }
                
		_watchedDirs.add( root );

	    }
	    catch ( IOException ioe ){
		ioe.printStackTrace();
	    }
	}
	
    }

    private void _checkHeads( File heads )
	throws IOException {
	if ( ! ( heads.exists() && heads.isDirectory() ) )
	    throw new RuntimeException( "heads has to exist and be a dir" );

	for ( File head : heads.listFiles() ){
	    if ( D ) System.out.println( "\t" + head.getName() );
	    _checkHead( head );
	}
    }

    private void _checkHead( File head )
	throws IOException {
	
	if ( _ignore( head.toString() ) )
	    return;
	
	if ( ! ( head.exists() && ! head.isDirectory() ) )
	    throw new RuntimeException( "head has to exist and NOT be a dir" );


	String name = getName( head );
	String branch = head.getName();
	String hash = StreamUtil.readFully( new FileInputStream( head ) ).trim();
	
	_ensureDB( name , branch , hash );
	
    }

    private boolean _ignore( String s ){
	if ( s.endsWith( ".lock" ) )
	    return true;
	
	return false;
    }

    private void _ensureDB( String repos , String branch , String hash ){
	Cloud.getInstance().evalFunc( "Git.ensureHash" , repos , branch , hash );
    }

    private void _watch( File root , File heads ){
	try {
	    JNotify.addWatch( heads.getAbsolutePath() , LISTEN_MASK , false , _changeListener );
	}
	catch ( JNotifyException e ){
	    throw new RuntimeException( "couldn't watch : " + heads , e );
	}
    }

    private List<File> findAllRepositories(){
	List<File> all = new ArrayList<File>();
	findAllRepositories( _gitroot , all );
	return all;
    }

    private void findAllRepositories( File f , List<File> all ){
	if ( ! f.exists() )
	    return;
	
	if ( ! f.isDirectory() )
	    return;

	File git = new File( f , ".git" );
	if ( git.exists() && git.isDirectory() ){
	    all.add( f );
	    return;
	}

	for ( File next : f.listFiles() )
	    findAllRepositories( next , all );
    }

    String getName( File f ){
	String s = f.getAbsolutePath();
	s = s.substring( _gitroot.getAbsolutePath().length() + 1 );
	if ( s.indexOf( ".git" ) >= 0 ){
	    s = s.substring( 0 , s.indexOf( ".git" ) - 1 );
	}

	return s;
    }

    class ChangeListener implements JNotifyListener {
	
	void _check( String dir ){
	    if ( D ) System.out.println( "Checking " + dir );
	    try {
		_checkHeads( new File( dir ) );
	    }
	    catch( Exception e ){
		_log.error( "couldn't check from notify : " + dir , e );
	    }
	}

	public void fileRenamed(int wd, String rootPath, String oldName, String newName){
	    if ( _ignore( newName ) )
		return;

	    _check( rootPath );
	}
	
	public void fileModified(int wd, String rootPath, String name){
	    if ( _ignore( name ) )
		return;
	    
	    _check( rootPath );
	}
	
	public void fileDeleted(int wd, String rootPath, String name){
	    if ( _ignore( name ) )
		return;
	    
	    _check( rootPath );
	    _log.error( "deletes not supported : " + rootPath + " " + name  );
	}
        
	public void fileCreated(int wd, String rootPath, String name){
	    if ( _ignore( name ) )
		return;

	    _check( rootPath );
	}
    }

    final File _gitroot;
    final File _sites;

    final Set<File> _watchedDirs = new HashSet<File>();
    final ChangeListener _changeListener = new ChangeListener();
    
    final Thread _fullChecker;
    
    static final int LISTEN_MASK = 
	JNotify.FILE_CREATED | 
	JNotify.FILE_DELETED | 
	JNotify.FILE_MODIFIED |
	JNotify.FILE_RENAMED;
	

    // ----

    public static void main( String args[] )
	throws Exception {

	GitMonitor gm = new GitMonitor();
	while ( true ){
	    Thread.sleep( 10000 );
	}
    }
	
}
