// JSAppender.java

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
import ed.js.engine.*;

public class JSAppender extends JSObjectWrapper implements Appender  {

    public JSAppender( JSFunction func ){
        super( func );
        _func = func;
    }

    public void append( Event e ){
        if ( _func.getNumParameters() > 1 )
            _func.call( Scope.getThreadLocal() , e._loggerName , e._date , e._level , e._msg , e._throwable , e._threadName , EMPTY );
        else 
            _func.call( Scope.getThreadLocal() , e , EMPTY );
    }

    final JSFunction _func;

    static final Object[] EMPTY = new Object[0];
}
