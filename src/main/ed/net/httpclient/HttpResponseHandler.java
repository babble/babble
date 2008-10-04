// HttpResponseHandler.java

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
