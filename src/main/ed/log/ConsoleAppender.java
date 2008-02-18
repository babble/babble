// ConsoleAppender.java

package ed.log;

public class ConsoleAppender implements Appender {

    public void append( String loggerName , ed.js.JSDate date , Level level , String msg , Throwable throwable , Thread thread ){
        System.out.println( "[" + date + "] " + loggerName + " [" + thread.getName() + "] " + level + " " + msg );
        if ( throwable != null )
            throwable.printStackTrace();
    }

}
