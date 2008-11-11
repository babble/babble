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

import java.nio.channels.*;

public class NIOUtil {

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
}
