// Cloud.java

package ed.cloud;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import ed.js.*;
import ed.js.engine.*;
import ed.js.func.*;
import ed.log.*;

public class Cloud extends JSObjectBase {

    static Logger _log = Logger.getLogger( "cloud" );

    private static final Cloud INSTANCE = new Cloud();

    public static Cloud getInstance(){
	return INSTANCE;
    }


    // ---

    private Cloud(){

	File cloudDir = new File( "src/main/ed/cloud/" );
	if ( ! cloudDir.exists() )
	    throw new RuntimeException( "can't find cloud dir" );

	_scope = Scope.GLOBAL.child( "cloud" );
	Shell.addNiceShellStuff( _scope );
	_scope.set( "Cloud" , this );
	_scope.set( "log" , _log );

	try {
	    _scope.set( "SERVER_NAME" , InetAddress.getLocalHost().getHostName() );
	}
	catch ( Exception e ){
	    throw new RuntimeException( "should be impossible : " + e );
	}
	
	List<File> toLoad = new ArrayList<File>();
	for ( File f : cloudDir.listFiles() ){

	    if ( ! f.getName().matches( "\\w+\\.js" ) )
		continue;
	    
	    toLoad.add( f );
	}

	final Matcher numPattern = Pattern.compile( "(\\d+)\\.js$" ).matcher( "" );
	Collections.sort( toLoad , new Comparator<File>(){
			      public int compare( File aFile , File bFile ){
				  int a = Integer.MAX_VALUE;
				  int b = Integer.MAX_VALUE;
				  
				  numPattern.reset( aFile.getName() );
				  if ( numPattern.find() )
				      a = Integer.parseInt( numPattern.group(1) );

				  numPattern.reset( bFile.getName() );
				  if ( numPattern.find() )
				      b = Integer.parseInt( numPattern.group(1) );

				  return a - b;
			      }

			      public boolean equals( Object o ){
				  return o == this;
			      }
			  } );

	for ( File f : toLoad ){
	    _log.debug( "loading file : " + f );
	    try {
		_scope.eval( f );
	    }
	    catch ( IOException ioe ){
		throw new RuntimeException( "can't load cloud js file : " + f , ioe );
	    }
	}
	
    }


    Object evalFunc( String funcName , Object ... args ){
	if ( args != null ){
	    for ( int i=0; i <args.length; i++ ){
		if ( args[i] instanceof String )
		    args[i] = new JSString( (String)args[i] );
	    }
	}
	
	
	JSFunction func = (JSFunction)findObject( funcName );
	if ( func == null )
	    throw new RuntimeException( "can't find func : " + funcName );

	return func.call( _scope , args );
    }

    Object findObject( String name ){

	if ( ! name.matches( "[\\w\\.]+" ) )
	    throw new RuntimeException( "this is to complex for my stupid code [" + name + "]" );
	
	String pcs[] = name.split( "\\." );
	Object cur = this;
	
	for ( int i=0; i<pcs.length; i++ ){
	
	    if ( i == 0 && pcs[i].equals( "Cloud" ) )
		continue;
	    
	    cur = ((JSObject)cur).get( pcs[i] );
	    if ( cur == null )
		return null;
	}
	return cur;
    }

    public Scope getScope(){
        return _scope;
    }

    final Scope _scope;
}
