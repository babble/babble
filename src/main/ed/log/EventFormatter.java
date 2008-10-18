// EventFormatter.java

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

import java.text.*;

import ed.js.*;

public interface EventFormatter {

    public String format( Event e );

    public static class DefaultEventFormatter implements EventFormatter {
        
        public synchronized String format( Event e ){
            _buf.append( "[" ).append( e._date.format( _format ) ).append( "] " );
            _buf.append( e._thread.getName() ).append( " || " ).append( e._loggerName ).append( " " );
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
        final SimpleDateFormat _format = new SimpleDateFormat( "MM/dd/yyyy hh:mm:ss.SSS z" );
    }
    
}
