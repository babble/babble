// DailyFileAppender.java

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
