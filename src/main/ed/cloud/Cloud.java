// Cloud.java

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
import java.net.*;
import java.util.*;
import java.util.regex.*;

import ed.db.DBAddress;
import ed.js.*;
import ed.js.engine.*;
import ed.log.*;

public class Cloud extends JSObjectBase {

    static final boolean FORCE_GRID = ed.util.Config.get().getBoolean( "FORCE-GRID" );

    static Logger _log = Logger.getLogger( "cloud" );
    static {
        _log.setLevel( Level.INFO );
    }

    private static final Cloud INSTANCE;
    static {
        
        Cloud c = null;
        
        try {
            c = new Cloud();
        }
        catch ( Throwable t ){
            t.printStackTrace();
            System.err.println( "couldn't load cloud - dying" );
            System.exit(-1);
        }
        finally {
            INSTANCE = c;
        }
    }

    public static synchronized Cloud getInstance(){
	return INSTANCE;
    }


    public static Cloud getInstanceIfOnGrid(){
        Cloud c = getInstance();
        if ( c == null )
            return null;
        
        if ( ! c.isOnGrid() )
            return null;
        
        return c;
    }

    // ---

    private Cloud(){

	File cloudDir = new File( "src/main/ed/cloud/" );
	_scope = Scope.newGlobal().child( "cloud" );
        
        _bad = ! cloudDir.exists();
        if ( _bad ){
            System.err.println( "NO CLOUD" );
            return;
        }


        _scope.set( "connect" , new Shell.ConnectDB() );
        _scope.set( "openFile" , new ed.js.func.JSFunctionCalls1(){
                public Object call( Scope s , Object fileName , Object crap[] ){
                    return new JSLocalFile( fileName.toString() );
                }
            } );

	_scope.set( "Cloud" , this );
	_scope.set( "log" , _log );

	try {
	    _scope.set( "SERVER_NAME" , System.getProperty( "SERVER-NAME" , InetAddress.getLocalHost().getHostName() ) );
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
	
	_log.info( "isOnGrid : " + isOnGrid() );

    }

    public String getDBHost( String dbname ){
        if ( _bad )
            return null;

	JSObject db = (JSObject)evalFunc( "Cloud.findDBByName" , dbname );
	if ( db == null )
	    throw new RuntimeException( "can't find global db named [" + dbname + "]" );
	
	Object machine = db.get( "machine" );
	if ( machine == null )
	    throw new RuntimeException( "global db [" + dbname + "] doesn't have machine set" );

        return machine.toString();
    }

    public DBAddress getDBAddressForSite( String siteName , String environment ){
        if ( _bad )
            return null;

        JSObject site = findSite( siteName , false );
        if ( site == null )
            return null;
        
        Object url = evalFunc( site , "getDBUrlForEnvironment" , environment );
        if ( url == null )
            return null;

        try {
            return new DBAddress( url.toString() );
        }
        catch ( UnknownHostException e ){
            throw new RuntimeException( "bad db url [" + url + "] " + e );
        }
    }

    public JSObject findSite( String name , boolean create ){
        if ( _bad )
            return null;
        return (JSObject)(evalFunc( "Cloud.Site.forName" , name , create ));
    }
    
    public JSObject findEnvironment( String name , String env ){
        if ( _bad ) 
            return null;
        
        JSObject s = findSite( name , false );
        if ( s == null )
            return null;
        
        return (JSObject)(evalFunc( s , "findEnvironmentByName" , env ) );
    }

    public Zeus createZeus( String host , String user , String pass )
        throws IOException {
        return new Zeus( host , user , pass );
    }

    Object evalFunc( String funcName , Object ... args ){
        return evalFunc( null , funcName , args );
    }
    
    Object evalFunc( JSObject t , String funcName , Object ... args ){
	
        if ( args != null ){
	    for ( int i=0; i <args.length; i++ ){
		if ( args[i] instanceof String )
		    args[i] = new JSString( (String)args[i] );
	    }
	}
	
        JSFunction func = null;
        
        if ( func == null && t != null ){
            func = (JSFunction)t.get( funcName );
        }

        if ( func == null )
            func = (JSFunction)findObject( funcName );

	if ( func == null )
	    throw new RuntimeException( "can't find func : " + funcName );
        
        Scope s = _scope;
        if ( t != null ){
            s = _scope.child();
            s.setThis( t );
        }

	return func.call( s , args );
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
    
    public boolean isOnGrid(){
        if ( FORCE_GRID )
            return true;

        if ( _bad )
            return false;
    
        JSObject me = (JSObject)(_scope.get("me"));
        if ( me == null )
	    throw new RuntimeException( "why doesn't me exist" );

        return ! JSInternalFunctions.JS_evalToBool( me.get( "bad" ) );
    }

    public String getModuleSymLink( String moduleName , String version ){
        if ( _bad )
            return null;
        
        Object res = evalFunc( "Cloud.getModuleSymLink" , moduleName , version );
        if ( res == null )
            return null;
        return res.toString();
    }
    
    public String toString(){
        return "{ Cloud.  ongrid: " + isOnGrid() + "}";
    }

    final Scope _scope;
    final boolean _bad;
}
