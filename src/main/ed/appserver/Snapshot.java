// Snapshot.java

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

package ed.appserver;

import java.util.*;

import ed.js.*;
import ed.js.engine.*;
import ed.util.*;

public class Snapshot {

    public Snapshot( String s ) {
        timestamp = System.currentTimeMillis();
        name = s;
    }

    public static Snapshot getAppContextSnapshot( AppContext ac ) {
        Snapshot s = new Snapshot( "AppContext" );
        Scope scope = ac._scope;

        Iterator<String> it = scope.allKeys().iterator();
        String str;
        while(it.hasNext()) {
            str = it.next();
            s.put( str, JSObjectSize.size( scope.get( str ) ) );
        }
        s.put( "total" , ac.approxSize() );

        return s;
    }

    public Iterator<String> iterator() {
        return _objs.keySet().iterator();
    }

    public Long get( String s ) {
        return _objs.get( s );
    }

    public Long put( String s, Long l ) {
        return _objs.put( s, l );
    }

    public static String compareSnapshots( Snapshot s1, Snapshot s2 ) {
        StringBuffer sb = new StringBuffer();
        sb.append( "<br />" + s1.name + " Memory Usage <table><tr>" +
                   "<th>Name</th>" + 
                   "<th>Current Size</th>" +
                   "<th>Snapshot Size" );

        if( s2 != null ) {
            sb.append( " (" + (new Date( s2.timestamp )) + ")" );
        }
        sb.append( "</th></tr>" );

        Iterator<String> i = s1.iterator();
        String s;

        // print fields
        while( i.hasNext() ) {
            s = i.next();
            if( s == "total" )
                continue;

            long l1 = s1.get( s ).longValue();
            long l2 = s2 == null ? -1 : s2.get( s ).longValue();
            if( l1 != l2 ) {
                sb.append( "<tr><td>" );
                sb.append( s );
                sb.append( "</td><td>" );
                sb.append( l1 );
                sb.append( "</td><td>" );
                if( s2 != null ) {
                    sb.append( l2 );
                }
                sb.append( "</td></tr>" );
            }
        }

        // print total
        sb.append( "<tr><td><b>total</b></td><td><b>" + s1.get( "total" ) +"</b></td><td><b>" );
        if( s2 != null ) {
            sb.append( s2.get( "total" ) );
        }
        sb.append( "</b></td></tr>" );

        sb.append( "</table>" );
        return sb.toString();
    }

    private Hashtable<String,Long> _objs = new Hashtable<String,Long>();
    public final String name;
    public final long timestamp;
}