// Appender.java

package ed.log;

import ed.js.*;

public interface Appender {

    public void append( String loggerName , JSDate date , Level level , String msg , Throwable throwable , Thread thread );

}
