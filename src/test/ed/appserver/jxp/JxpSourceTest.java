// JxpSourceTest.java

package ed.appserver.jxp;

import java.io.*;

import org.testng.annotations.Test;

public class JxpSourceTest extends ed.TestCase {
    
    static class StringSource extends JxpSource {
        StringSource( String s ){
            _s = s;
        }

        public File getFile(){
            return null;
        }
        
        String getName(){
            return "temp.jxp";
        }

        String getContent() {
            return _s;
        }

        InputStream getInputStream(){
            return new ByteArrayInputStream( _s.getBytes() );
        }
        
        public long lastUpdated(){
            return _t;
        }

        final String _s;
        final long _t = System.currentTimeMillis();
    }


    public static void main( String args[] ){
        (new JxpSourceTest()).runConsole();
    }
}
