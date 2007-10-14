// Source.java

package ed.appserver.jxp;

import java.io.*;
import java.util.*;

import ed.io.*;
import ed.util.*;

abstract class JxpSource {

    static JxpSource getSource( File f ){
        return new JxpFileSource( f );
    }
    
    abstract String getContent() throws IOException;
    abstract long lastUpdated();
    
    List<Block> getBlocks()
        throws IOException {
        if ( _blocks == null || _lastParse < lastUpdated() )
            _blocks = Parser.parse( this );
        return _blocks;
    }
    

    private List<Block> _blocks;
    private long _lastParse = 0;
            
    static class JxpFileSource extends JxpSource {
        JxpFileSource( File f ){
            _f = f;
        }

        String getContent()
            throws IOException {
            return StreamUtil.readFully( _f );
        }
        
        long lastUpdated(){
            return _f.lastModified();
        }

        final File _f;
    }
}
