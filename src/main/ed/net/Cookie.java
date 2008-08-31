// Cookie.java

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

package ed.net;

import java.util.*;
import java.text.*;
import javax.servlet.http.*;

/**
   maxAge:
     < 0 : remove
     0 : session
     > 0 : now + X seconds
     
 */
public class Cookie extends javax.servlet.http.Cookie {

    public Cookie( String name , String value ){
        this( name , value , 0 );
    }

    public Cookie( String name , String value , int maxAge ){
        this( null , name , value , maxAge );
    }

    public Cookie( String domain , String name , String value ){
        this( domain , name , value , 0 );
    }

    public Cookie( String domain , String name , String value , int maxAge ){
        super( name , value );
        setDomain( domain );
        setMaxAge( maxAge );
    }

    public String getExpires(){
        if ( getMaxAge() < 0 )
            return REMOVE_COOKIE_EXPIRES;
        if ( getMaxAge() == 0 )
            return null;
        synchronized ( COOKIE_DATE_FORMAT ){
            return COOKIE_DATE_FORMAT.format( new java.util.Date( System.currentTimeMillis() + ( 1000L * (long)getMaxAge() ) ) );
        }
    }
    
    public void setExpiryDate( Date when ){
        long diff = when.getTime() - System.currentTimeMillis();
        setMaxAge( (int)(diff / 1000) );
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
