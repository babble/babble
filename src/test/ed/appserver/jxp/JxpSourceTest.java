// JxpSourceTest.java

package ed.appserver.jxp;

import java.io.*;
import java.util.Set;

import org.testng.annotations.Test;

import ed.util.Dependency;

public class JxpSourceTest extends ed.TestCase {
    
    static class StringSource extends JxpSource {
        StringSource( String s ){
            _s = s;
        }

        public File getFile(){
            return null;
        }
        
        protected String getName(){
            return "temp.jxp";
        }

        protected String getContent() {
            return _s;
        }

        protected InputStream getInputStream(){
            return new ByteArrayInputStream( _s.getBytes() );
        }
        
        public long lastUpdated(Set<Dependency> visitedDeps){
            visitedDeps.add(this);
            return _t;
        }

        final String _s;
        final long _t = System.currentTimeMillis();
    }


    public static void main( String args[] ){
        (new JxpSourceTest()).runConsole();
    }
}
