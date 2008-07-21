// JSAppender.java

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
import ed.js.engine.*;

public class JSAppender extends JSObjectWrapper implements Appender  {

    public JSAppender( JSFunction func ){
        super( func );
        _func = func;
    }

    public void append( String loggerName , JSDate date , Level level , String msg , Throwable throwable , Thread thread ){
        _func.call( Scope.getThreadLocal() , loggerName , date , level , msg , throwable , thread , EMPTY );
    }

    final JSFunction _func;

    static Object[] EMPTY = new Object[0];
}
