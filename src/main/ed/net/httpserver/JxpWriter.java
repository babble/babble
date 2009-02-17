// JxpWriter.java

/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

package ed.net.httpserver;

public interface JxpWriter extends Appendable {

    public boolean closed();

    public JxpWriter print( String s );
    public JxpWriter print( int i );
    public JxpWriter print( double d );
    public JxpWriter print( long l );
    public JxpWriter print( boolean b );

    /**
     * this has the same semantics as OutputStream.write( int b )
     * so b is a byte, not an int
     */
    public void write( int b );
		      
    
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

        public boolean closed(){
            return false;
        }

        public Appendable append(char c){
            _buf.append( c );
            return this;
        }
        
        public Appendable append(CharSequence csq){
            _buf.append( csq );
            return this;
        }
        public Appendable append(CharSequence csq, int start, int end){
            _buf.append( csq , start , end );
            return this;
        }
        
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

	public void write( int b ){
	    _buf.append( (char)( (byte)( b & 0xFF ) ) );
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
