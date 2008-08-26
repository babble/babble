// HttpResponseHandlerBase.java

package ed.net.httpclient;

import java.net.*;
import java.util.*;

import ed.util.*;
import ed.net.httpserver.*;

/**
*/
public abstract class HttpResponseHandlerBase implements HttpResponseHandler {

    public void gotHeader( String name , String value )
	throws java.io.IOException {
	_receivedHeaders.put( name , value );
    }

    public void removeHeader( String name ){
        _receivedHeaders.remove( name );
    }

    public void gotResponseCode( int responseCode ){
	_responseCode = responseCode;
    }

    public int getContentLength() { return _contentLength; }

    public void gotContentLength( int contentLength ){
	_contentLength = contentLength;
    }

    public void gotContentEncoding( String contentEncoding ){
	_contentEncoding = contentEncoding;
    }

    public void gotCookie( Cookie c ){
	_cookies.add( c );
    }

    public void setFinalUrl( URL url ){
	_finalUrl = url;
    }

    public URL getFinalUrl() {
        return _finalUrl;
    }

    public void addHeader( String n , String v ){
	_headersToSend.put( n , v );
    }

    public void setAuthentication( String username , String password ){
	addHeader( "Authorization" ,
		   " Basic " + Base64.encodeBytes( new String( username+":"+password ).getBytes() ) );
    }

    public String getHeader( String n ) {
	return( _receivedHeaders.get( n ) );
    }

    public long getDesiredTimeout(){
        return -1;
    }

    public boolean followRedirect( URL url ){
        _redirectUrls.add( url );
        return true;
    }

    public Map<String,String> getHeadersToSend(){ return _headersToSend; };
    public Map<String,Cookie> getCookiesToSend(){ return null; };

    public byte[] getPostDataToSend(){ return null; };

    protected Map<String,String> _headersToSend = new StringMap<String>();

    protected Map<String,String> _receivedHeaders = new StringMap<String>();
    protected int _responseCode;
    protected int _contentLength;
    protected String _contentEncoding;
    protected List<Cookie> _cookies = new ArrayList<Cookie>();
    protected URL _finalUrl;
    protected List<URL> _redirectUrls = new ArrayList<URL>();
}
