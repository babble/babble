// Cloud.java

package ed.cloud;

import java.io.*;
import java.util.*;

import ed.js.*;
import ed.js.engine.*;
import ed.js.func.*;

public class Cloud extends JSObjectBase {

    private static final Cloud INSTANCE = new Cloud();

    public static Cloud getInstance(){
	return INSTANCE;
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

    private Cloud(){

	File cloudDir = new File( "src/main/ed/cloud/" );
	if ( ! cloudDir.exists() )
	    throw new RuntimeException( "can't find cloud dir" );

	_scope = Scope.GLOBAL.child( "cloud" );
	_scope.set( "Cloud" , this );

	
	for ( File f : cloudDir.listFiles() ){
	    
	    if ( ! f.getName().matches( "\\w+\\.js" ) )
		continue;

	    System.out.println( "cloud js file : " + f );
	    try {
		_scope.eval( f );
	    }
	    catch ( IOException ioe ){
		throw new RuntimeException( "can't load cloud js file : " + f , ioe );
	    }
	}
	
    }


    final Scope _scope;
}