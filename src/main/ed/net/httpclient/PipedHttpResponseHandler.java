// PipedHttpResponseHandler.java

package ed.net.httpclient;

import java.io.*;

import ed.io.*;
import ed.util.*;

/**
*/
public class PipedHttpResponseHandler extends HttpResponseHandlerBase {
    public PipedHttpResponseHandler( OutputStream out ){
	this( out , -1 );
    }
    
    public PipedHttpResponseHandler( OutputStream out , int maxSize ){
	_out = out;
	_maxSize = maxSize;
    }

    public int read( InputStream in )
	throws IOException {
	return StreamUtil.pipe( in , _out , _maxSize );
    }

    OutputStream _out;
    int _maxSize = -1;
}
