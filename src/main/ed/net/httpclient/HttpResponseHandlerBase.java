// HttpResponseHandlerBase.java

/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

package ed.net.httpclient;

import java.net.*;
import java.util.*;
import javax.servlet.http.*;

import ed.util.*;
import ed.net.*;

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

    public boolean wantHttpErrorExceptions() {
        return true;
    }

    public Map<String,String> getHeadersToSend(){ return _headersToSend; };
    public Map<String,Cookie> getCookiesToSend(){ return null; };

    public byte[] getPostDataToSend(){ return null; };

    public String getMethodToUse() { return "GET"; };

    protected Map<String,String> _headersToSend = new StringMap<String>();

    protected Map<String,String> _receivedHeaders = new StringMap<String>();
    protected int _responseCode;
    protected int _contentLength;
    protected String _contentEncoding;
    protected List<Cookie> _cookies = new ArrayList<Cookie>();
    protected URL _finalUrl;
    protected List<URL> _redirectUrls = new ArrayList<URL>();
}
