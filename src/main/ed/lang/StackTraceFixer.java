// StackTraceFixer.java

package ed.lang;

public interface StackTraceFixer {

    /**
     * @return null or the same if not changed.  or a new element
     */
    public StackTraceElement fixSTElement( StackTraceElement element );

    /**
     * @return true if we should remove this element from the stack trace
     */
    public boolean removeSTElement( StackTraceElement element );
}
