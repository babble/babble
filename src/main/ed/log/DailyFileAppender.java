// DailyFileAppender.java

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

import ed.js.*;

public class DailyFileAppender implements Appender {

    public DailyFileAppender( String base ){
        this( null , base );
    }

    public DailyFileAppender( File dir , String base ){
        this( dir , base , new EventFormatter.DefaultEventFormatter() );
    }

    public DailyFileAppender( File dir , String base , EventFormatter formatter ){
        if ( dir == null )
            dir = new File( "logs/" );
        _dir = dir;
        _dir.mkdirs();
        _base = base;
        _formatter = formatter;
    }
    
    public void append( Event e ){
        try {
            _check();
            _printStream.write( _formatter.format( e ).getBytes( "utf8" ) );
        }
        catch ( IOException ioe ){
            ioe.printStackTrace();
        }
    }
    
    private void _check()
        throws IOException {

        if ( _printStream != null && System.currentTimeMillis() < _nextSwitch )
            return;
        
        if ( _printStream != null )
            _printStream.close();
        
        JSDate now = (new JSDate()).roundDay();
        _nextSwitch = now.getTime() + ( 1000 * 3600 * 24 );

        String name = _base + "." + now.format( "yyyy-MM-dd" );
        File f = new File( _dir , name );

        _printStream = new PrintStream( new FileOutputStream( f , true ) );
    }
    
    final File _dir;
    final String _base;
    final EventFormatter _formatter;

    private long _nextSwitch = 0;
    private PrintStream _printStream;
}
