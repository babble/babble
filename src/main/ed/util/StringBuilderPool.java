// StringBuilderPool.java

package ed.util;

/** @expose */
public class StringBuilderPool extends SimplePool<StringBuilder> {

    /** Initializes a pool of a given number of StringBuilders, each of a certain size.
     * @param maxToKeep the number of string builders in the pool
     * @param maxSize the size of each string builder
     */
    public StringBuilderPool( int maxToKeep , int maxSize ){
        super( "StringBuilderPool" , maxToKeep , -1  );
        _maxSize = maxSize;
    }

    /** Create a new string builder.
     * @return the string builder
     */
    public StringBuilder createNew(){
        return new StringBuilder();
    }

    /** Checks that the given string builder is within the size limit.
     * @param buf the builder to check
     * @return if it is not too big
     */
    public boolean ok( StringBuilder buf ){
        if ( buf.length() > _maxSize )
            return false;
        buf.setLength( 0 );
        return true;
    }

    /** @unexpose */
    final int _maxSize;
}
