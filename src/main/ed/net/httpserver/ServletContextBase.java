// ServletContextBase.java

package ed.net.httpserver;

import java.util.*;
import javax.servlet.*;

import ed.js.*;
import ed.log.*;
import ed.util.*;
import ed.appserver.*;

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
}
