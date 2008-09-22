// PHPEnv.java

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

package ed.lang.php;

import com.caucho.quercus.*;
import com.caucho.quercus.env.*;
import com.caucho.quercus.page.*;
import com.caucho.quercus.parser.*;
import com.caucho.quercus.program.*;
import com.caucho.vfs.ReaderStream;
import com.caucho.vfs.WriteStream;
import com.caucho.vfs.ReaderWriterStream;

import bak.pcj.map.*;

import ed.js.*;
import ed.appserver.*;
import ed.net.httpserver.*;

class PHPEnv extends Env {
    
    PHPEnv( Quercus quercus , QuercusPage page , WriteStream out , AppRequest ar ){
	super( quercus , page , out , ar.getRequest() , ar.getResponse() );
	_request = ar.getRequest();
	_response = ar.getResponse();
        _appRequest = ar;
    }

    public HttpRequest getRequest(){
        return _request;
    }

    public HttpResponse getResponse(){
        return _response;
    }
    
    public Var getSpecialRef(String name){
        
	if ( name.equals( "_GET" ) )
            return _toVar( _request.getURLParameters() );
        
	if ( name.equals( "_POST" ) )
            return _toVar( _request.getPostParameters() );

        if ( name.equals( "_COOKIE" ) )
            return _toVar( _request.getCookiesObject() );

        if ( name.equals( "_SESSION" ) )
            return _toVar( _appRequest.getSession() );
        
        if ( name.equals( "_SERVER" ) )
            return _toVar( new ServerObject() );

	return super.getSpecialRef( name );
    }

    public SessionArrayValue getSession(){
	return _stupidSession;
    }

    Var _toVar( JSObject obj ){
        Var v = new Var();
        v.set( _convertor.toPHP( obj ) );
        return v;
    }

    public Value wrapJava( Object obj ){
        _convertor.checkConfigged( obj );
        if ( obj instanceof JSObject )
            return (Value)(_convertor.toOther( obj ) );
        return super.wrapJava( obj );
    }
    
    class ServerObject extends JSObjectLame {
        public Object get( Object thing ){
            final String name = thing.toString();

            if ( name.equals( "REQUEST_URI" ) )
                return _request.getURI();

            if ( name.equals( "REQUEST_METHOD" ) )
                return _request.getMethod();

            if ( name.equals( "REQUEST_TIME" ) )
                return (int)(_startTime / 1000);

            
            if ( name.equals( "QUERY_STRING" ) )
                return _request.getQueryString();


            
            if ( name.equals( "HTTP_HOST" ) )
                return _request.getHeader( "Host" );
            
            if ( name.startsWith( "HTTP_" ) )
                return _request.getHeader( name.substring(5).replace( '_' , '-' ) );

            if ( name.equals( "HTTPS" ) )
                return null;
            
            if ( name.equals( "REMOTE_ADDR" ) || name.equals( "REMOTE_HOST" ) )
                return _request.getRemoteIP();


            if ( name.equals( "SERVER_PORT" ) ){
                int port = _request.getPort();
                if ( port == 0 )
                    return 80;
                return port;
            }
            
            if ( name.equals( "SERVER_NAME" ) || name.equals( "SERVER_SOFTWARE" ) )
                return "10gen querces php";
            
            if ( name.equals( "SERVER_PROTOCOL" ) )
                return _request.getProtocol();

            if ( name.equals( "PHP_SELF" ) )
                return _request.getFullPath();
            
            System.err.println( "ServerObject can't handle [" + name + "]" );
            return null;
        }
    }

    final AppRequest _appRequest;    
    final HttpRequest _request;
    final HttpResponse _response;
    final long _startTime = System.currentTimeMillis();

    PHPConvertor _convertor;

    static class MyStupidSession extends SessionArrayValue {
	MyStupidSession(){
	    super( "asd" , 123 , 123 );
	}
    }

    private static final MyStupidSession _stupidSession = new MyStupidSession();
}
