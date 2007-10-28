// DBJni.java

package ed.db;

public class DBJni {

    static {
        System.load( ( new java.io.File( "build/libdb.so" ) ).getAbsolutePath() );
    }
    
    public static native void test1();

    public static void main( String args[] ){
        test1();
    }
    
}
