// PrintStreamAppender.java

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

package ed.log;

import java.io.*;
import java.text.*;

public class PrintStreamAppender implements Appender {

    public PrintStreamAppender( PrintStream out ){
        this( out , new EventFormatter.DefaultEventFormatter() );
    }

    public PrintStreamAppender( PrintStream out , EventFormatter formatter ){
        _out = out;
        _formatter = formatter;
    }

    public void append( final Event e ){
	String output = _formatter.format( e );
	
        try {
            _out.write( output.getBytes( "utf8" ) );
            if ( e._throwable != null )
                e._throwable.printStackTrace( _out );
        }
        catch ( IOException encodingBad ){
            _out.print( output );
        }
	catch ( Exception we ){
	    _out.print( output );
            we.printStackTrace();
	}	
    }

    final PrintStream _out;
    final EventFormatter _formatter;
}
