// JxpWriter.java

package ed.net.httpserver;

public interface JxpWriter {

    public JxpWriter print( String s );
    public JxpWriter print( int i );
    public JxpWriter print( double d );
    public JxpWriter print( boolean b );
    
    public void flush()
        throws java.io.IOException ;
    public void reset();
    
}
