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

import ed.net.httpserver.*;

class PHPEnv extends Env {
    
    PHPEnv( Quercus quercus , QuercusPage page , WriteStream out , HttpRequest request , HttpResponse response ){
	super( quercus , page , out , null , null );
	_request = request;
	_response = response;
    }
    
    public Var getSpecialRef(String name){
	
	if ( name.equals( "_GET" ) ){
	    if ( _get == null ){
		_get = new Var();
		_get.set( _convertor.toPHP( _request.getURLParameters() ) );
	    }
	    return _get;
	}

	if ( name.equals( "_POST" ) ){
	    if ( _post == null ){
		_post = new Var();
		_post.set( _convertor.toPHP( _request.getPostParameters() ) );
	    }
	    return _post;
	}

	System.out.println( "unhandled special [" + name + "]" );
	return super.getSpecialRef( name );
    }
    
    final HttpRequest _request;
    final HttpResponse _response;

    PHPConvertor _convertor;

    Var _get;
    Var _post;
}
