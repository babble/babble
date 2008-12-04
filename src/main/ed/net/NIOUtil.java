// NIOUtil.java

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
import java.nio.channels.*;

public class NIOUtil {

    public static String toString( Collection<SelectionKey> coll ){
	StringBuilder buf = new StringBuilder( "[");
	
	for ( SelectionKey key : coll )
	    buf.append( toString( key ) ).append(", " );

	return buf.append( "]" ).toString();
    }

    public static String toString( SelectionKey key ){

        StringBuilder buf = new StringBuilder();

        buf.append( "{SelectionKey.  " );

        buf.append( key.channel() ).append( " " );

        if ( key.isAcceptable() )
            buf.append( " acceptable " );

        if ( key.isConnectable() )
            buf.append( " connectable " );
        
        if ( key.isReadable() )
            buf.append( " readable " );

        if ( key.isWritable() )
            buf.append( " writable " );
        
        buf.append( "}" );
        return buf.toString();
    }

    public static String readyOps( int i ){
        if ( i == 0 )
            return "none";
        
        StringBuilder buf = new StringBuilder();

        if ( ( i & SelectionKey.OP_ACCEPT ) > 0 )
            buf.append( "accept " );
        if ( ( i & SelectionKey.OP_CONNECT ) > 0 )
            buf.append( "connect " );
        if ( ( i & SelectionKey.OP_READ ) > 0 )
            buf.append( "read " );
        if ( ( i & SelectionKey.OP_WRITE ) > 0 )
            buf.append( "write " );

        return buf.toString();
    }
}
