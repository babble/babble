// PrintStreamAppender.java

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
        }
        catch ( IOException encodingBad ){
            _out.print( output );
        }
        if ( e._throwable != null )
            e._throwable.printStackTrace( _out );
    }

    final PrintStream _out;
    final EventFormatter _formatter;
}
