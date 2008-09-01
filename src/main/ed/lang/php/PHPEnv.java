// PHPEnv.java

package ed.lang.php;

import com.caucho.quercus.*;
import com.caucho.quercus.env.*;
import com.caucho.quercus.page.*;
import com.caucho.quercus.parser.*;
import com.caucho.quercus.program.*;
import com.caucho.vfs.ReaderStream;
import com.caucho.vfs.WriteStream;
import com.caucho.vfs.ReaderWriterStream;

import ed.js.*;
import ed.appserver.*;
import ed.net.httpserver.*;

class PHPEnv extends Env {
    
    PHPEnv( Quercus quercus , QuercusPage page , WriteStream out , AppRequest ar ){
	super( quercus , page , out , null , ar.getResponse() );
	_request = ar.getRequest();
	_response = ar.getResponse();
        _appRequest = ar;
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
            return _toVar( _request.getCookies() );

        if ( name.equals( "_SESSION" ) )
            return _toVar( _appRequest.getSession() );
        
	return super.getSpecialRef( name );
    }

    Var _toVar( JSObject obj ){
        Var v = new Var();
        v.set( _convertor.toPHP( obj ) );
        return v;
    }

    final AppRequest _appRequest;    
    final HttpRequest _request;
    final HttpResponse _response;

    
    PHPConvertor _convertor;
}
