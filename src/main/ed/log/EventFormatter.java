// EventFormatter.java

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

import java.text.*;

import ed.js.*;

public interface EventFormatter {

    public String format( Event e );

    public static class DefaultEventFormatter implements EventFormatter {
        
        public synchronized String format( Event e ){
            _buf.append( "[" ).append( e._date.format( _format ) ).append( "] " );
            _buf.append( e.getThreadName() ).append( " || " ).append( e._loggerName ).append( " " );
            _buf.append( e._level ).append( " >> " ).append( e._msg ).append( "\n" );
            
            String s = _buf.toString();
            if ( _buf.length() > _bufSize )
                _buf = new StringBuilder( _bufSize );
            else
                _buf.setLength( 0 );
            return s;
        }
        
        final int _bufSize = 512;
        private StringBuilder _buf = new StringBuilder( _bufSize );
        final SimpleDateFormat _format = new SimpleDateFormat( "MM/dd/yyyy HH:mm:ss.SSS z" );
    }
    
}
