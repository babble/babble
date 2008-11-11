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

/**
*  Portions of this code taken from Apache's HTTPClient and was licensed under the following terms :
*  
*  
*  Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  See the NOTICE file distributed with
*  this work for additional information regarding copyright ownership.
*  The ASF licenses this file to You under the Apache License, Version 2.0
*  (the "License"); you may not use this file except in compliance with
*  the License.  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation.  For more
* information on the Apache Software Foundation, please see
* <http://www.apache.org/>.
* 
*/

package ed.js;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

public class JSCookieJar extends JSObjectBase {
    //TODO: add clean method to remove stale & optionally nonpr
    public JSCookieJar() {
        this._creationDates = new HashMap<String, Date>();
    }

    /**
     * Validates & adds cookies to this object
     * 
     * @param source the origin server of the cookie
     * @param cookie the being added 
     */
    public void addCookie(URL source, Cookie cookie) {
        try {
            validate( source , cookie );
        }
        catch( MalformedCookieException e ) {
            //TODO: invalid cookies
            return;
        }
        catch( IllegalArgumentException e ) {
            //TODO: invalid cookies
            return;
        }
        
        if( cookie.getMaxAge() == 0 ) {
            removeField( cookie.getName() );
            return;
        }
        else {
            set( cookie.getName(), cookie );
        }
    }

    /**
     * Returns all applicable cookies for the given url.
     * @param requestingUrl
     * @return
     */
    public List<Cookie> getActiveCookies(URL requestingUrl) {
        JSArray cookiesToSend = new JSArray();
        
        JSObjectValueIterator iter = new JSObjectValueIterator( this );
        while( iter.hasNext() ) {
            Cookie c = (Cookie) iter.next();
            
            if( match( requestingUrl, c ) )
                cookiesToSend.add( c );
        }

        return cookiesToSend;
    }
    
    public List<Cookie> clean() {
        return clean( true );
    }
    
    public List<Cookie> clean(boolean removeNonpersistent) {
        List<Cookie> deadCookies = new ArrayList<Cookie>();

        JSObjectValueIterator iter = new JSObjectValueIterator( this );
        while( iter.hasNext() ) {
            Cookie c = (Cookie) iter.next();
            
            if( isExpired( c ) )
                deadCookies.add( c );
            
            if( removeNonpersistent && c.getMaxAge() < 0 )
                deadCookies.add( c );
        }
        for(Cookie deadCookie : deadCookies)
            removeField( deadCookie.getName() );
        
        return deadCookies;
    }
    /**
     * Allows cookies to be directly added to the jar
     */
    public Object set(Object n, Object v) {
        if( ! ( v instanceof Cookie ) )
            throw new IllegalArgumentException( "This object can only contain cookies" );
        
        String key = n.toString();
        Cookie c = (Cookie)v;
        
        if( !key.equals( c.getName() ) )
            throw new IllegalArgumentException( "Key/cookie name mismatch, expected the cookie name to be " + key + ", but was " +c.getName() );
        
        _creationDates.put( key , new Date() );
        return super.set( key , v );
    }
    
    public Object removeField(Object n) {
        _creationDates.remove( n.toString() );
        return super.removeField( n );
    }

    /**
     * Performs RFC 2109 {@link Cookie} validation
     * 
     * @param url the source of the cookie
     * @param cookie The cookie to validate.
     * @throws IllegalArgumentException if an exception occurs during validation
     */
    private void validate(URL url, Cookie cookie) {
        String host = url.getHost();
        int port = url.getPort();
        String path = url.getPath();

        // based on org.apache.commons.httpclient.cookie.CookieSpecBase
        if (host == null) {
            throw new IllegalArgumentException( "Host of origin may not be null" );
        }
        if (host.trim().equals( "" )) {
            throw new IllegalArgumentException( "Host of origin may not be blank" );
        }
        if (port < 0) 
            port = 80;
            
        if (path == null) {
            throw new IllegalArgumentException( "Path of origin may not be null." );
        }
        if (path.trim().equals( "" )) {
            path = "/";
        }
        host = host.toLowerCase();
        // check version
        if (cookie.getVersion() < 0) {
            throw new MalformedCookieException( "Illegal version number " + cookie.getValue() );
        }

        // security check... we musn't allow the server to give us an
        // invalid domain scope

        // Validate the cookies domain attribute. NOTE: Domains without
        // any dots are allowed to support hosts on private LANs that don't
        // have DNS names. Since they have no dots, to domain-match the
        // request-host and domain must be identical for the cookie to sent
        // back to the origin-server.
        if (host.indexOf( "." ) >= 0) {
            // Not required to have at least two dots. RFC 2965.
            // A Set-Cookie2 with Domain=ajax.com will be accepted.

            // domain must match host
            if (!host.endsWith( cookie.getDomain() )) {
                String s = cookie.getDomain();
                if (s.startsWith( "." )) {
                    s = s.substring( 1 , s.length() );
                }
                if (!host.equals( s )) {
                    throw new MalformedCookieException( "Illegal domain attribute \"" + cookie.getDomain()
                            + "\". Domain of origin: \"" + host + "\"" );
                }
            }
        } else {
            if (!host.equals( cookie.getDomain() )) {
                throw new MalformedCookieException( "Illegal domain attribute \"" + cookie.getDomain()
                        + "\". Domain of origin: \"" + host + "\"" );
            }
        }

        // another security check... we musn't allow the server to give us a
        // cookie that doesn't match this path
        if (!path.startsWith( cookie.getPath() )) {
            throw new MalformedCookieException( "Illegal path attribute \"" + cookie.getPath() + "\". Path of origin: \"" + path + "\"" );
        }

        // Validate using RFC 2109
        // --------------------------------------------------------
        if (cookie.getName().indexOf( ' ' ) != -1) {
            throw new MalformedCookieException( "Cookie name may not contain blanks" );
        }
        if (cookie.getName().startsWith( "$" )) {
            throw new MalformedCookieException( "Cookie name may not start with $" );
        }

        if (cookie.getDomain() != null && (!cookie.getDomain().equals( host ))) {

            // domain must start with dot
            if (!cookie.getDomain().startsWith( "." )) {
                throw new MalformedCookieException( "Domain attribute \"" + cookie.getDomain()
                        + "\" violates RFC 2109: domain must start with a dot" );
            }
            // domain must have at least one embedded dot
            int dotIndex = cookie.getDomain().indexOf( '.' , 1 );
            if (dotIndex < 0 || dotIndex == cookie.getDomain().length() - 1) {
                throw new MalformedCookieException( "Domain attribute \"" + cookie.getDomain()
                        + "\" violates RFC 2109: domain must contain an embedded dot" );
            }
            host = host.toLowerCase();
            if (!host.endsWith( cookie.getDomain() )) {
                throw new MalformedCookieException( "Illegal domain attribute \"" + cookie.getDomain()
                        + "\". Domain of origin: \"" + host + "\"" );
            }
            // host minus domain may not contain any dots
            String hostWithoutDomain = host.substring( 0 , host.length() - cookie.getDomain().length() );
            if (hostWithoutDomain.indexOf( '.' ) != -1) {
                throw new MalformedCookieException( "Domain attribute \"" + cookie.getDomain()
                        + "\" violates RFC 2109: host minus domain may not contain any dots" );
            }
        }
    }
    
    /**
     * Return <tt>true</tt> if the cookie should be submitted with a request
     * with given attributes, <tt>false</tt> otherwise.
     * @param destination the destination of the request
     * @param cookie {@link Cookie} to be matched
     * @return true if the cookie matches the criterium
     */
    private boolean match(URL destination, final Cookie cookie) {
        String host = destination.getHost();
        int port = destination.getPort();
        String path = destination.getPath();
        boolean secure = "https".equals( destination.getProtocol() );
        
        if (host == null) {
            throw new IllegalArgumentException( "Host of origin may not be null" );
        }
        if (host.trim().equals( "" )) {
            throw new IllegalArgumentException( "Host of origin may not be blank" );
        }
        if (port < 0) {
            port = 80;
        }
        if (path == null) {
            throw new IllegalArgumentException( "Path of origin may not be null." );
        }
        if (cookie == null) {
            throw new IllegalArgumentException( "Cookie may not be null" );
        }
        if (path.trim().equals( "" )) {
            path = "/";
        }
        host = host.toLowerCase();
        if (cookie.getDomain() == null) {
            return false;
        }
        if (cookie.getPath() == null) {
            return false;
        }

        return
        // only add the cookie if it hasn't yet expired
        !isExpired( cookie )
        // and the domain pattern matches
        && (domainMatch( host , cookie.getDomain() ))
        // and the path is null or matching
        && (pathMatch( path , cookie.getPath() ))
        // and if the secure flag is set, only if the request is
        // actually secure
        && (cookie.getSecure() ? secure : true);
    }
    /**
     * Performs domain-match as implemented in common browsers.
     * @param host The target host.
     * @param domain The cookie domain attribute.
     * @return true if the specified host matches the given domain.
     */
    private boolean domainMatch(final String host, String domain) {
        if (host.equals(domain)) {
            return true;
        }
        if (!domain.startsWith(".")) {
            domain = "." + domain;
        }
        return host.endsWith(domain) || host.equals(domain.substring(1));
    }

    /**
     * Performs path-match as implemented in common browsers.
     * @param path The target path.
     * @param topmostPath The cookie path attribute.
     * @return true if the paths match
     */
    private boolean pathMatch(final String path, final String topmostPath) {
        boolean match = path.startsWith (topmostPath);
        // if there is a match and these values are not exactly the same we have
        // to make sure we're not matcing "/foobar" and "/foo"
        if (match && path.length() != topmostPath.length()) {
            if (!topmostPath.endsWith("/")) {
                match = (path.charAt(topmostPath.length()) == '/');
            }
        }
        return match;
    }
    
    /**
     * Checks if the cookie has expired
     * @param cookie the cookie to check
     * @return true, if the cookie has an expiration date that has been reached
     */
    private boolean isExpired( Cookie cookie ) {
        if( cookie.getMaxAge() < 0)
            return false;
        
        if( cookie.getMaxAge() == 0)
            return true;
        
        Date createDate = _creationDates.get( cookie.getName() );
        Date expirationDate = new Date( createDate.getTime() + ( cookie.getMaxAge() * 1000 ) );
        
        return expirationDate.getTime() <= System.currentTimeMillis();
    }
    

    private final Map<String, Date> _creationDates;
    

    public static class MalformedCookieException extends RuntimeException {
        public MalformedCookieException() {
            super();
        }
         
        /** 
         * Creates a new MalformedCookieException with a specified message string.
         * 
         * @param message The exception detail message
         */
        public MalformedCookieException(String message) {
            super(message);
        }

        /**
         * Creates a new MalformedCookieException with the specified detail message and cause.
         * 
         * @param message the exception detail message
         * @param cause the <tt>Throwable</tt> that caused this exception, or <tt>null</tt>
         * if the cause is unavailable, unknown, or not a <tt>Throwable</tt>
         * 
         */
        public MalformedCookieException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
