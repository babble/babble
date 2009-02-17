// Cookie.java

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

import java.util.*;
import java.text.*;
import javax.servlet.http.*;

/**
   maxAge:
     < 0 : session
     0 : remove
     > 0 : now + X seconds
     
 */
public class CookieUtil {

    public static String getExpires( Cookie c ){
        if ( c.getMaxAge() == 0 )
            return REMOVE_COOKIE_EXPIRES;
        if ( c.getMaxAge() < 0 )
            return null;
        synchronized ( COOKIE_DATE_FORMAT ){
            return COOKIE_DATE_FORMAT.format( new java.util.Date( System.currentTimeMillis() + ( 1000L * (long)c.getMaxAge() ) ) );
        }
    }

    public static int getMaxAge( Date when ){
        long diff = when.getTime() - System.currentTimeMillis();
        return (int)(diff / 1000);
    }

    public final static DateFormat COOKIE_DATE_FORMAT =
	new SimpleDateFormat( "EEE, dd-MMM-yyyy HH:mm:ss z" , Locale.US);
    static {
	COOKIE_DATE_FORMAT.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
    }
    static final String REMOVE_COOKIE_EXPIRES = COOKIE_DATE_FORMAT.format( new Date( 10000 ) );

    public static String formatToSend( Collection<Cookie> cookies ){
        return formatToSend( cookies.iterator() );
    }

    public static String formatToSend( Iterator<Cookie> cookies ){
        StringBuilder buf = new StringBuilder();

        while ( cookies.hasNext() ){

            if ( buf.length() > 0 )
                buf.append( "; " );

            Cookie c = cookies.next();
            buf.append( c.getName() ).append( "=" ).append( c.getValue() );
        }
        return buf.toString();
    }
}
