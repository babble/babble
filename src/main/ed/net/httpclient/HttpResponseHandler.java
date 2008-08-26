// HttpResponseHandler.java

package ed.net.httpclient;

import java.io.*;
import java.net.*;
import java.util.*;

import ed.net.httpserver.*;

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
    
    public Map<String,String> getHeadersToSend();
    public Map<String,Cookie> getCookiesToSend();
    public byte[] getPostDataToSend();
    /**
       -1 for default
       ms otherwise
    */
    public long getDesiredTimeout();
}
