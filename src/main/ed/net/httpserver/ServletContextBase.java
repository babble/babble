// ServletContextBase.java

package ed.net.httpserver;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import ed.js.*;
import ed.js.engine.*;
import ed.log.*;
import ed.util.*;
import ed.appserver.*;
import ed.appserver.jxp.*;

/**
 * convenience class to make implementing a ServletContext easy
 */
public abstract class ServletContextBase implements ServletContext {
    
    protected ServletContextBase( String name ){
	_baseName = name;
	_attributes = new JSObjectBase();
	_logger = Logger.getLogger( name );
    }

    public Object getAttribute(String name){
	return _attributes.get( name );
    }
    public Enumeration getAttributeNames(){
	return new CollectionEnumeration( _attributes.keySet() );
    }
    public void removeAttribute(String name){
	_attributes.removeField( name );
    }
    public void setAttribute(String name, Object object){
	_attributes.set( name , object );
    }

    public ServletContext getContext(String uripath){
	return this;
    }

    public void log(Exception exception, String msg){
	log( msg , exception );
    }

    public void log(String msg){
	_logger.info( msg );
    }
    
    public void log(String message, Throwable throwable){
	_logger.error( message , throwable );
    }

    public String getServletContextName(){
	return _baseName;
    }

    public int getMajorVersion(){
	return 2;
    }
    
    public int getMinorVersion(){
	return 2;
    }

    public String getMimeType(String file){
	return MimeTypes.get( file );
    }
    
    public String getServerInfo(){
	return "10gen server";
    }
    
    public Servlet getServlet(String name){
	throw new RuntimeException( "deprecated" );
    }
    public Enumeration getServletNames(){
	throw new RuntimeException( "deprecated" );
    }
    public Enumeration getServlets(){
	throw new RuntimeException( "deprecated" );
    }

    public final Logger getLogger(){
	return _logger;
    }
    
    final String _baseName;
    final JSObject _attributes;
    final protected Logger _logger;

    public class ServletSource extends JxpSource {

	public ServletSource( HttpServlet servlet ){
	    _servlet = servlet;
	    _name = _servlet.getClass().getName();

	    _func = new ed.js.func.JSFunctionCalls0(){
		    public Object call( Scope s , Object extra[] ){
			try {
			    AppRequest ar = (AppRequest)(s.get( "__apprequest__" ));
			    _servlet.service( ar.getRequest() , ar.getResponse() );
			    return null;
			}
			catch ( Exception e ){
			    throw new RuntimeException( "error handling HttpServlet : " + _servlet , e );
			}
		    }
		};
	}
	
	protected String getContent(){
	    throw new RuntimeException( "can't getContent from a ServletSource" );
	}
	
	protected InputStream getInputStream(){
	    throw new RuntimeException( "can't getInputStream from a ServletSource" );
	}
	
	public long lastUpdated(Set<Dependency> visitedDeps){
	    return _created;
	}
	
	public String getName(){
	    return _name;
	}

	public File getFile(){
	    return null;
	}
	
	public JSFunction getFunction(){
	    return _func;
	}
	
	final HttpServlet _servlet;
	final String _name;
	final long _created = System.currentTimeMillis();
	final JSFunction _func;
    }
}
