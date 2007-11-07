// DNSUtil.java

package ed.net;

public class DNSUtil {

    public static String getDomain( String host ){
        String tld = getTLD( host );
        int idx = host.lastIndexOf( "." , host.length() - ( tld.length() + 2 ) );
        if ( idx < 0 )
            return host;
        return host.substring( idx + 1 );
    }

    public static String getTLD( String host ){
        
        int idx = host.lastIndexOf( "." );
        if ( idx < 0 )
            throw new RuntimeException( "not a host:" + host );

        // TODO: 2nd level domains
        return host.substring( idx + 1 );
    }

}
