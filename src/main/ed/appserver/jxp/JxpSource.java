// Source.java

package ed.appserver.jxp;

import java.io.*;
import java.util.*;
import java.util.regex.*;

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
                jsCode += "\n print( \"\\n\" );";
                
                temp = File.createTempFile( "jxp_js_" , ".js" );
                _lastFileName = temp.toString();
                
                FileOutputStream fout = new FileOutputStream( temp );
                fout.write( jsCode.getBytes() );
                fout.close();
                
                try {
                    Convert c = new Convert( temp );
                    _func = c.get();
                }
                catch ( Exception e ){
                    System.out.println( e );
                    throw new RuntimeException( "couldn't compile [" + getName() + "] : " + getCompileMessage( e ) );
                }
            }
            finally {
                if ( temp != null && temp.exists() ){
                    temp.delete();
                }
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

    public void fix( Throwable t ){
        if ( _jsCodeToLines == null )
            return;
        
        //System.out.println( _jsCodeToLines );

        StackTraceElement stack[] = t.getStackTrace();
        
        boolean changed = false;
        for ( int i=0; i<stack.length; i++ ){
            
            StackTraceElement element = stack[i];
            if ( element == null )
                continue;
            
            String es = element.toString();
            if ( ! es.contains( _lastFileName ) )
                continue;
            
            int line = StringParseUtil.parseInt( es.substring( es.lastIndexOf( ":" ) + 1 ) , -1 );
            
            stack[i] = new StackTraceElement( getName() , stack[i].getMethodName() , getName() , getSourceLine( line ) );
            changed = true;
            System.out.println( stack[i] );
            
        }
        
        if ( ! changed )
            return;
        
        t.setStackTrace( stack );
    }

    public String getCompileMessage( Exception e ){
        String msg = e.getMessage();
        
        Matcher m = Pattern.compile( "^(.+) \\(.*#(\\d+)\\)$" ).matcher( msg );
        if ( ! m.find() )
            return msg;
        
        return m.group(1) + "  Line Number : " + getSourceLine( 1 + Integer.parseInt( m.group(2) ) );
    }
    
    public int getSourceLine( int line ){
        List<Block> blocks = _jsCodeToLines.get( line );
        
        if ( blocks == null || blocks.size() == 0 )
            return line;
        
        int thisBlockStart = line - 1;
        while ( thisBlockStart >= 0 ){
            List<Block> temp = _jsCodeToLines.get( thisBlockStart );
            if ( temp == null || temp.size() == 0 )
                break;
            if ( ! temp.contains( blocks.get(0) ) )
                break;
            thisBlockStart--;
        }
        thisBlockStart++;
        
        return blocks.get( 0 )._lineno + ( line - thisBlockStart );
    }

    private long _lastParse = 0;
    
    private List<Block> _blocks;
    private JSFunction _func;
    private JxpServlet _servlet;
    
    Map<Integer,List<Block>> _jsCodeToLines = new TreeMap<Integer,List<Block>>();
    String _lastFileName;

    // -------------------
    
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
