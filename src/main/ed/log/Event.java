// Event.java

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

import ed.js.*;

public class Event {

    public Event( String loggerName , JSDate date , Level level , String msg , Throwable throwable , Thread thread ){
        _loggerName = loggerName;
        _date = date;
        _level = level;
        _msg = msg;
        _throwable = throwable;
        _threadName = thread == null ? null : thread.getName();
    }
    
    public String getLoggerName(){
        return _loggerName;
    }

    public JSDate getDate(){
        return _date;
    }

    public Level getLevel(){
        return _level;
    }

    public String getMsg(){
        return _msg;
    }

    public Throwable getThrowable(){
        return _throwable;
    }

    public String getThreadName(){
        return _threadName;
    }
    
    final String _loggerName;
    final JSDate _date;
    final Level _level;
    final String _msg;
    final Throwable _throwable;
    final String _threadName;


}
