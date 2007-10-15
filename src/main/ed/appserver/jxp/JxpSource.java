// Source.java

package ed.appserver.jxp;

import java.io.*;
import java.util.*;

import ed.io.*;
import ed.util.*;
import ed.js.*;
import ed.js.engine.*;

public abstract class JxpSource {

    public static JxpSource getSource( File f ){
        return new JxpFileSource( f );
    }

    // -----

    abstract String getContent() throws IOException;
    abstract long lastUpdated();
    
    synchronized List<Block> getBlocks()
        throws IOException {
        _checkTime();
        if ( _blocks == null ){
            _lastParse = lastUpdated();
            _blocks = Parser.parse( this );
        }
        return _blocks;
    }

    public synchronized JSFunction getFunction()
        throws IOException {
        
        _checkTime();

        if ( _func == null ){
            File temp = null;
            try {
                String jsCode = Generator.genJavaScript( getBlocks() );
                
                System.out.println( jsCode );

                temp = File.createTempFile( "jxp_js_" , ".js" );
                FileOutputStream fout = new FileOutputStream( temp );
                fout.write( jsCode.getBytes() );
                fout.close();
                
                Convert c = new Convert( temp );
                _func = c.get();
            }
            finally {
                temp.delete();
            }
        }
        return _func;
    }


    public JxpServlet getServlet()
        throws IOException {
        _checkTime();
        if ( _servlet == null )
            _servlet = new JxpServlet( this , getFunction() );
        return _servlet;
    }
    

    private void _checkTime(){
        if ( _lastParse >= lastUpdated() )
            return;
        
        _blocks = null;
        _func = null;
        _servlet = null;
    }


    private long _lastParse = 0;
    
    private List<Block> _blocks;
    private JSFunction _func;
    private JxpServlet _servlet;
            
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
