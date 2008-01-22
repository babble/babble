// Cookie.java

package ed.net.httpserver;

import java.util.*;
import java.text.*;

/**
   maxAge:
     < 0 : remove
     0 : session
     > 0 : now + X seconds
     
 */
public class Cookie {

    public Cookie( String name , String value ){
        this( name , value , 0 );
    }
    
    public Cookie( String name , String value , int maxAge ){
        _name = name;
        _value = value;
        _maxAge = maxAge;
    }

    public String getExpires(){
        if ( _maxAge < 0 )
            return REMOVE_COOKIE_EXPIRES;
        if ( _maxAge == 0 )
            return null;
        synchronized ( COOKIE_DATE_FORMAT ){
            return COOKIE_DATE_FORMAT.format( new java.util.Date( System.currentTimeMillis() + ( 1000L * (long)_maxAge ) ) );
        }
    }

    final String _name;
    final String _value;
    final int _maxAge;

    String _path = "/";


    public final static DateFormat COOKIE_DATE_FORMAT =
	new SimpleDateFormat( "EEE, dd-MMM-yyyy HH:mm:ss z" , Locale.US);
    static {
	COOKIE_DATE_FORMAT.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
    }
    static final String REMOVE_COOKIE_EXPIRES = COOKIE_DATE_FORMAT.format( new Date( 10000 ) );
    
}
