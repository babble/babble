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
    abstract String getName();
    
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
                Generator g = Generator.genJavaScript( getBlocks() );
                _jsCodeToLines = g._jsCodeToLines;
                String jsCode = g.toString();
                
                System.out.println( jsCode );

                temp = File.createTempFile( "jxp_js_" , ".js" );
                _lastFileName = temp.toString();
                
                FileOutputStream fout = new FileOutputStream( temp );
                fout.write( jsCode.getBytes() );
                fout.close();
                
                Convert c = new Convert( temp );
                _func = c.get();
            }
            finally {
                if ( temp != null && temp.exists() )
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
    
    Map<Integer,List<Block>> _jsCodeToLines = new TreeMap<Integer,List<Block>>();
    String _lastFileName;
            
    static class JxpFileSource extends JxpSource {
        JxpFileSource( File f ){
            _f = f;
        }

        String getName(){
            return _f.toString();
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
