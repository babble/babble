// GitMonitor.java

package ed.cloud;

import java.io.*;
import java.util.*;

import ed.io.*;

public class GitMonitor {

    static boolean D = Boolean.getBoolean( "DEBUG.GIT" );

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
    }

    
    private void fullCheck(){

	for ( File root : findAllRepositories() ){
	    if ( _watchedDirs.contains( root ) )
		continue;
	    
	    try {
		if ( D ) System.out.println( "repos : " + root );
		
		File heads = new File( root , ".git/refs/heads" );
		_watch( root , heads );
		
		for ( File head : heads.listFiles() ){
		    if ( D ) System.out.println( "\t" + head.getName() );
		    _checkHead( head );
		}

		throw new RuntimeException("boring" );
	    }
	    catch ( IOException ioe ){
		ioe.printStackTrace();
	    }
	}
	
    }

    private void _checkHead( File head )
	throws IOException {
	
	String name = getName( head );
	String branch = head.getName();
	String hash = StreamUtil.readFully( new FileInputStream( head ) );
	
	_ensureDB( name , branch , hash );
	
    }

    private void _ensureDB( String repos , String branch , String hash ){
	Cloud.getInstance().evalFunc( "Git.ensureHash" , repos , branch , hash );
    }

    private void _watch( File root , File heads ){
	System.err.println( "can't watch yet" );
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

    final File _gitroot;
    final File _sites;
    final Set<File> _watchedDirs = new HashSet<File>();

    // ----

    public static void main( String args[] )
	throws Exception {

	GitMonitor gm = new GitMonitor();
	
    }
	
}