// HttpResponseHandler.java

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

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.http.*;

import ed.net.*;

/**
 *
 */
public interface HttpResponseHandler {
    /** maybe should have a method that says get handler for input stream
        and read staright to that
        @return the number of bytes read
    */
    public int read( InputStream is )
        throws IOException ;

    public void gotHeader( String name , String value )
        throws IOException;
    public void removeHeader( String name );

    public void gotResponseCode( int responseCode );
    public void gotContentLength( int contentLength );
    public void gotContentEncoding( String contentEncoding );
    public void gotCookie( Cookie c );
    public void setFinalUrl( URL url );

    /** Called if there was a redirect to a url that is not unfollow.
        @see ShopUrl#followRedirect(ShopUrl)
    */
    public boolean followRedirect( URL url );

    /**
     * If true, we want statuses in the 400s and 500s to be exceptions.
     */
    public boolean wantHttpErrorExceptions();

    /**
     * Returns the method to use for the request, or null to use the default method.
     */
    public String getMethodToUse();

    public Map<String,String> getHeadersToSend();
    public Map<String,Cookie> getCookiesToSend();
    public byte[] getPostDataToSend();
    /**
       -1 for default
       ms otherwise
    */
    public long getDesiredTimeout();
}
