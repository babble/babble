// NIOUtil.java

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
