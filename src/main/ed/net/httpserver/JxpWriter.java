// JxpWriter.java

package ed.net.httpserver;

public interface JxpWriter {

    public JxpWriter print( String s );
    public JxpWriter print( int i );
    public JxpWriter print( double d );
    public JxpWriter print( long l );
    public JxpWriter print( boolean b );
    
    public void flush()
        throws java.io.IOException ;
    
    public void reset();

    public String getContent();

    // for going back - doesn't work yet
    public void mark( int mark );
    public void clearToMark();
    public String fromMark();

    // for putting content in before
    public void saveSpot();
    public void backToSpot();
    public void backToEnd();
    public boolean hasSpot();

    // ----------

    public static class Basic implements JxpWriter {
        
        public JxpWriter print( String s ){
            _buf.append( s );
            return this;
        }

        public JxpWriter print( int i ){
            _buf.append( i );
            return this;
        }
        
        public JxpWriter print( double d ){
            _buf.append( d );
            return this;
        }

        public JxpWriter print( long l ){
            _buf.append( l );
            return this;
        }
        
        public JxpWriter print( boolean b ){
            _buf.append( b );
            return this;
        }
        
        public void flush()
            throws java.io.IOException {
        }

        public void reset(){
            _buf.setLength( 0 );
        }
        
        public String getContent(){
            return _buf.toString();
        }

        public void mark( int mark ){
            _mark = mark;
        }
        
        public void clearToMark(){
            _buf.setLength( _mark );
        }
        
        public String fromMark(){
            return _buf.substring( _mark , _buf.length() );
        }

        public void saveSpot(){
            throw new RuntimeException( "not implemented yet" );
        }
        public boolean hasSpot(){
            throw new RuntimeException( "not implemented yet" );
        }
        public void backToSpot(){
            throw new RuntimeException( "not implemented yet" );
        }
        public void backToEnd(){
            throw new RuntimeException( "not implemented yet" );
        }

        final StringBuilder _buf = new StringBuilder();
        private int _mark;
    }
}
