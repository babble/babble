// Event.java

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

import ed.js.*;
import ed.util.*;

public class Event implements Sizable {

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

    public long approxSize( SeenPath seen ){
        return 
            JSObjectSize.size( _loggerName , seen , this ) + 
            JSObjectSize.size( _date , seen , this ) + 
            JSObjectSize.size( _level , seen , this ) + 
            JSObjectSize.size( _msg , seen , this ) + 
            JSObjectSize.size( _throwable , seen , this ) + 
            JSObjectSize.size( _threadName , seen , this );
    }
    
    final String _loggerName;
    final JSDate _date;
    final Level _level;
    final String _msg;
    final Throwable _throwable;
    final String _threadName;


}
