// HttpClient.java

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

import java.net.*;
import java.io.*;
import java.util.*;
import javax.servlet.http.*;

import bak.pcj.*;
import bak.pcj.map.*;

import ed.io.*;
import ed.log.*;
import ed.net.*;
import ed.util.*;

/**
 */
public class HttpClient {

    private final static long START_TIME = System.currentTimeMillis();
    private static boolean TRACK_USAGE = true;
    private final static IntKeyDoubleMap USAGE = new IntKeyDoubleOpenHashMap();

    private final static Logger LOGGER = Logger.getLogger( "ed.net.httpclient.HttpClient" );
    public final static boolean DEBUG = Boolean.getBoolean( "DEBUG.HTTP" );
    static Level DEBUG_LEVEL = DEBUG  ? Level.INFO : Level.DEBUG;

    public static String USER_AGENT = "10gen http client";

    public static int MAX_REDIRECTS = 30;

    public static final boolean TRACE = Boolean.getBoolean("HTTP.TRACE");

    /**
       returns size of content
       if handler is null, does a head request and returns Content-Length header if exists
       if handler is not null, writes response information to handler

       @return size of content
    */
    public static int download( URL url , HttpResponseHandler handler )
        throws IOException {
        if ( DEBUG ) LOGGER.log( DEBUG_LEVEL , "download called on:" + url + " handler is " + (handler == null ? "null" : "not null"   ) );
        int numRedirects = 0;

        Set<String> setHeaders = new HashSet<String>();

        HttpConnection conn = setUpConnection( url , handler , numRedirects , setHeaders );
        int rc = conn.getResponseCode();

        while ( rc >= 300 && rc < 400 && numRedirects++ < MAX_REDIRECTS ){
            String loc = conn.getHeaderField("Location");
            if ( loc == null ) {
                throw new IOException("Response Code:" + rc + " and no Location header" );
            }
            handler.gotResponseCode( rc );
            addResponseHeaders( url , handler , conn , setHeaders );

            URL redir = new URL( url, loc.trim() );

            if ( DEBUG ) LOGGER.log( DEBUG_LEVEL , "redirect to: " + redir );
            if ( handler != null && ! handler.followRedirect( redir ) ) {
                url = redir;
                if ( DEBUG ) System.out.println( "not redirected to : " + redir + " by " + url.getClass() + " : " + url );
                break;
            }


            if ( loc.toLowerCase().indexOf("404.htm") >= 0 )
                throw new FileNotFoundException( url.toString() );

            url = redir;

            conn.done();
            conn = setUpConnection( url , handler , numRedirects , setHeaders );
            rc = conn.getResponseCode();
        }

        if ( handler != null )
            handler.setFinalUrl( url );

        if ( DEBUG ) LOGGER.log( DEBUG_LEVEL , url + ":" + String.valueOf(rc) );

        int contentLength = conn.getContentLength();

        if ( handler != null ){
            handler.gotResponseCode( rc );
            handler.gotContentLength( contentLength );
            if ( conn.getContentEncoding() != null )
                handler.gotContentEncoding( conn.getContentEncoding() );
            addResponseHeaders( url , handler , conn , setHeaders );
            int t = handler.read( conn.getInputStream() );
            if ( contentLength <= 0 )
                contentLength = t;
            if ( TRACK_USAGE ){
                incrementUsage( Math.max( t , contentLength ) );
            }

        }

        conn.done();

        if ( handler == null || handler.wantHttpErrorExceptions() ) {
            if ( rc == 404 )
                throw new FileNotFoundException( url.toString() );

            if ( rc >= 400 )
                throw new IOException("Error Code:" + String.valueOf(rc) );
        }

        return contentLength;
    }

    static final int now(){
        return (int)( (System.currentTimeMillis() - START_TIME) / 1000 );
    }

    static final void incrementUsage( int num ){
        final int n = now();
        synchronized ( USAGE ){
            USAGE.put( n , num + USAGE.get(n) );
        }
    }

    protected static HttpConnection setUpConnection( URL url , HttpResponseHandler handler , int number , Set<String> setHeaders )
        throws IOException {

        String urlString = url.toString();

        _allDownloads.write( urlString );

        if ( TRACE ) System.out.println( "Downloading: " + url );

        //HttpURLConnection conn = DNSUtil.openConnection( url.getURL() );
        HttpConnection conn = HttpConnection.get( url );

        if ( handler != null && setHeaders != null && setHeaders.size() > 0 ){
            for ( String h : setHeaders ){
                if ( h.equalsIgnoreCase("content-encoding") )
                    handler.removeHeader( h );
            }
        }

        if ( handler == null )
            conn.setRequestMethod("HEAD");

        // should these go before or after Page code.
        // basically, do we want Page do be able to override
        // if yes, then put before, if not, then put after
        conn.setRequestProperty( "User-Agent" , USER_AGENT );
        conn.setRequestProperty( "Connection" , "Keep-Alive" );
        conn.setRequestProperty( "Accept" , "*/*" );
        conn.setRequestProperty( "Accept-Language" , "en-US" );

        if ( handler != null ){

            if ( handler.getPostDataToSend() != null && number == 0 ){
                conn.setRequestMethod("POST");
                conn.setPostData( handler.getPostDataToSend() );
            } else {
                conn.setPostData(null);
            }

            // use the handler's method if it's set
            if (handler.getMethodToUse() != null) {
                if (handler.getMethodToUse() == "POST" && number != 0) {
                    conn.setRequestMethod("GET");
                } else {
                    conn.setRequestMethod(handler.getMethodToUse());
                }
            }

            Map headers = handler.getHeadersToSend();
            if ( headers != null ){
                for ( Iterator i = headers.keySet().iterator() ; i.hasNext();){
                    String name = (String)i.next();
                    String value = (String)headers.get(name);
                    if ( value == null || value.length() == 0 )
                        continue;
                    conn.setRequestProperty( name , value );
                }
            }

            Map<String,Cookie> cookies = handler.getCookiesToSend();
            if ( cookies != null && cookies.size() > 0 )
                conn.setRequestProperty( "Cookie" , CookieUtil.formatToSend( cookies.values() ) );

        }

        conn.go();
        return conn;
    }

    private static void addResponseHeaders( URL url , HttpResponseHandler handler , HttpConnection conn , Set<String> setHeaders )
        throws IOException {

        if ( handler == null || handler.getMethodToUse() == "HEAD") // HEAD reqeuest
            return;

        for ( int i=0; i<100; i++){
            String name = conn.getHeaderFieldKey( i );
            String value = conn.getHeaderField( i );

            if ( name == null && i > 0  )
                break;

            if ( value != null ){

                if ( name == null )
                    name = "FIRST_LINE";

                //System.err.println("name:" + name + " value:" + value );

                handler.gotHeader( name , value );
                setHeaders.add( name );

                // special cookie stuff
                if ( name.equalsIgnoreCase( "Set-Cookie" ) ){
                    try {
                        Cookie c = parseCookie( url.getHost() , url.getFile() , false , value );
                        //System.out.println( c );
                        handler.gotCookie( c );
                    }
                    catch ( Exception e ){
                        // this is their fault, not mine
                        LOGGER.debug("bad host url:" + url + " value:" + value , e );
                    }
                }

            }


        }
    }

    public static Cookie parseCookie( String domain , String file , boolean secure , String header ){

        Cookie c = null;

        int start = 0;
        int semiIndex;
        do {
            semiIndex = header.indexOf( ";" , start );

            int eqIndex = header.indexOf("=",start);
            if ( semiIndex > 0 && eqIndex > semiIndex )
                eqIndex = -1;

            String name = header.substring( start , eqIndex < 0 ? header.length() : eqIndex ).trim();
            if ( name.length() == 0 )
                break;
            String value = null;
            if ( eqIndex > 0 )
                value = header.substring( eqIndex + 1 , semiIndex > 0 ? semiIndex : header.length() ).trim();

            if ( c == null ){
                c = new Cookie( name , value  );
                c.setDomain( domain );
                c.setSecure( true );
                c.setPath("/");
            }
            else if ( name.equalsIgnoreCase("path") )
                c.setPath( value );
            else if ( name.equalsIgnoreCase("domain") )
                c.setDomain( value );
            else if ( name.equalsIgnoreCase("expires") ){
                try {
                    c.setMaxAge( CookieUtil.getMaxAge( CookieUtil.COOKIE_DATE_FORMAT.parse( value ) ) );
                }
                catch ( Exception e ){
                    if ( DEBUG ) LOGGER.log( DEBUG_LEVEL , "couldn't parse date : " + value );
                }
            }

            if ( semiIndex >= 0 )
                start = semiIndex + 1;
        } while ( semiIndex >= 0 );

        return c;
    }

    public static boolean exists( URL url ){
        try {
            return download( url , null ) > 0 ;
        }
        catch ( IOException ioe ){
            return false;
        }
    }


    public static byte[] downloadBinary( URL url )
        throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PipedHttpResponseHandler handler = new PipedHttpResponseHandler( out );
        download( url , handler );
        return out.toByteArray();

    }

    static final RollingNamedPipe _allDownloads = new RollingNamedPipe( "http-download" );
    static {
        _allDownloads.setMessageDivider( "\n" );
    }

}
