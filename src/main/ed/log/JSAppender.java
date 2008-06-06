// JSAppender.java

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
