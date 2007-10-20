// StringBuilderPool.java

package ed.util;

public class StringBuilderPool extends SimplePool<StringBuilder> {

    public StringBuilderPool( int maxToKeep , int maxSize ){
        super( "StringBuilderPool" , maxToKeep , -1  );
        _maxSize = maxSize;
    }

    public StringBuilder createNew(){
        return new StringBuilder();
    }

    public boolean ok( StringBuilder buf ){
        if ( buf.length() > _maxSize )
            return false;
        buf.setLength( 0 );
        return true;
    }    

    final int _maxSize;
}
