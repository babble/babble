// DNSUtil.java

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

package ed.net;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

public class DNSUtil {

    public static final String IP_PATTERN = "(\\d+\\.\\d+\\.\\d+\\.\\d+)";
    public static final Pattern IP_PATTERN_COMPILED = Pattern.compile( IP_PATTERN );
    
    public static final boolean isDottedQuad( String s ){
        return IP_PATTERN_COMPILED.matcher( s ).matches();
    }

    public static String getSubdomain( String host ){
        String domain = getDomain( host );
        if ( host.length() == domain.length() )
            return "www";
        String sub = host.substring( 0 , host.length() - domain.length() );
        while ( sub.endsWith( "." ) )
            sub = sub.substring( 0 , sub.length() - 1 );
        sub = sub.trim();
        if ( sub.length() == 0 )
            return "www";
        return sub;
    }

    public static String getDomain( String host ){
        String tld = getTLD( host );
        int idx = host.lastIndexOf( "." , host.length() - ( tld.length() + 2 ) );
        if ( idx < 0 )
            return host;
        return host.substring( idx + 1 );
    }

    public static String getJustDomainName( String host ){
        String tld = getTLD( host );
        host = host.substring( 0 , host.length() - ( tld.length() + 1 ) );

        int idx = host.lastIndexOf( "." );
        if ( idx < 0 )
            return host;
        return host.substring( idx + 1 );
    }
    
    public static String getTLD( String host ){
        
        host = host.toLowerCase();

        // efficiency hack
        if ( host.endsWith( ".com" ) )
            return "com";
        
        int idx = host.lastIndexOf( "." );

        if ( idx < 0 )
            return "";

        if ( idx == host.length() - 1 )
            idx = host.lastIndexOf( "." , host.length() - 2 );
        
        if ( idx < 0 )
            return "";

        String tld = host.substring( idx + 1 );
        
        if ( TLDS.contains( tld ) )
            return tld;
        
        int idx2 = host.lastIndexOf( "." , idx - 1 );
        String moreTLD = idx2 < 0 ? host : host.substring( idx2 + 1 );

        if ( TLDS.contains( moreTLD ) )
            return moreTLD;

        return "";
    }
    
    public static List<InetAddress> getPublicAddresses()
        throws IOException {
        return getAddresses( false );
    }   

    public static List<InetAddress> getMyAddresses()
        throws IOException {
        return getAddresses( true );
    }


    public static List<InetAddress> getAddresses( boolean includeInternal )
        throws IOException {
        List<InetAddress> lst = new ArrayList<InetAddress>();
        
        for ( NetworkInterface nic : getNetworkInterfaces() ){
            Enumeration<InetAddress> e = nic.getInetAddresses();
            while ( e.hasMoreElements() ){
                InetAddress ia = e.nextElement();

                if ( ! ( ia instanceof Inet4Address ) )
                    continue;

                String ip = ia.getHostAddress();
                
                if ( ip.startsWith( "127.0.0.1" ) )
                    continue;

                if ( ! includeInternal ){
                    if ( ip.startsWith( "10." ) )
                        continue;
                    if ( ip.startsWith( "192.168." ) )
                        continue;
                }
                
                lst.add( ia );
            }
        }
        
        return lst;
    }

    public static boolean isLocalAddressSafe( String address ){
        try {
            return isLocalAddress( address );
        }
        catch ( IOException ioe ){
            return false;
        }
    }

    public static boolean isLocalAddress( String address )
        throws IOException {
        if ( address == null )
            return false;
        return isLocalAddress( getByName( address ) );
    }
    
    public static boolean isLocalAddress( InetAddress ia )
        throws IOException {
        
        for ( NetworkInterface nic : getNetworkInterfaces() ){
            Enumeration<InetAddress> e = nic.getInetAddresses();
            while ( e.hasMoreElements() ){
                InetAddress mine = e.nextElement();            
                if ( mine.getHostAddress().equals( ia.getHostAddress() ) )
                    return true;
            }
        }
        
        return false;
    }

    public static List<NetworkInterface> getNetworkInterfaces()
        throws java.io.IOException {
        List<NetworkInterface> lst = new ArrayList<NetworkInterface>();
        
        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
        while ( e.hasMoreElements() )
            lst.add( e.nextElement() );

        return lst;
    }

    final static Set<String> TLDS = new HashSet<String>();
    static {
        for ( String s : new ed.io.LineReader( ClassLoader.getSystemClassLoader().getResourceAsStream( "tlds.txt" ) ) ){
            s = s.toLowerCase().trim();
            if ( s.length() == 0 )
                continue;
            s = s.replaceAll( "\\-\\-.*" , "" );
            TLDS.add( s );
        }
    }

    public static InetAddress getByName( String host )
        throws UnknownHostException {
	
	if ( host == null )
	    throw new NullPointerException( "can't lookup null host" );

        return InetAddress.getByName( host );
    }

    public static final InetAddress getLocalHost(){
        return LOCALHOST;
    }

    public static final String getLocalHostString(){
        return LOCALHOST_STRING;
    }

    private final static InetAddress LOCALHOST;
    private final static String LOCALHOST_STRING;
    static {
        InetAddress l = null;
	try {
	    l = InetAddress.getLocalHost();
	}
	catch ( Exception e ){
	    e.printStackTrace();
	    System.err.println( "exiting" );
	    System.exit(-1);
	}
        finally {
            LOCALHOST = l;
            LOCALHOST_STRING = l == null ? "unknown host" : l.toString();
        }
    }
    
    public static void main( String args[] )
        throws Exception {
        
        System.out.println( getPublicAddresses() );
    }
    
}
