// Source.java

package ed.appserver.jxp;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import ed.io.*;
import ed.util.*;
import ed.js.*;
import ed.js.engine.*;
import ed.appserver.*;

public abstract class JxpSource {

    static final File _tmpDir = new File( "/tmp/jxp/s/" + Math.random() + "/" );
    static {
        _tmpDir.mkdirs();
    }

    public static JxpSource getSource( File f ){
        if ( f == null )
            throw new NullPointerException( "can't have null file" );
        return new JxpFileSource( f );
    }

    // -----

    abstract String getContent() throws IOException;
    abstract InputStream getInputStream() throws IOException ;
    public abstract long lastUpdated();
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
            File jsFile = null;
            String extension = MimeTypes.getExtension( getName() );
            try {
                
                if ( extension.equals( "js" ) 
                     || extension.equals( "jxp" )
                     || extension.equals( "html" )
                     ){
                    Generator g = Generator.genJavaScript( getBlocks() );
                    _jsCodeToLines = g._jsCodeToLines;
                    _jsCode = g.toString();
                    if ( ! getName().endsWith( ".js" ) )
                        _jsCode += "\n print( \"\\n\" );";
                }
                else if ( extension.equals( "rb" ) ){
                    ed.lang.ruby.RubyConvert rc = new ed.lang.ruby.RubyConvert( getName() , getInputStream() );
                    _jsCode = rc.getJSSource();
                }
                else {
                    throw new RuntimeException( "unkown extension [" + extension + "]" );
                }
                
                jsFile = new File( _tmpDir , getName().replaceAll( "[^\\w]" , "_" ) + ".js" );
                _lastFileName = jsFile.getName();
                
                FileOutputStream fout = new FileOutputStream( jsFile );
                fout.write( _jsCode.getBytes() );
                fout.close();
                
                try {
                    _convert = new Convert( jsFile );
                    _func = _convert.get();
                }
                catch ( Exception e ){
                    System.out.println( e );
                    throw new RuntimeException( "couldn't compile [" + getName() + "] : " + getCompileMessage( e ) );
                }
            }
            finally {
                if ( jsFile != null && jsFile.exists() ){
                    //jsFile.delete();
                }
            }
        }
        return _func;
    }
    

    public JxpServlet getServlet( AppContext context )
        throws IOException {
        _checkTime();
        if ( _servlet == null )
            _servlet = new JxpServlet( context , this , getFunction() );
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

        if ( _convert != null )
            _convert.fixStack( t );

        if ( _jsCodeToLines != null ){
            
            StackTraceElement stack[] = t.getStackTrace();
            
            boolean changed = false;
            for ( int i=0; i<stack.length; i++ ){
                
                StackTraceElement element = stack[i];
                if ( element == null )
                    continue;
                
                String es = element.toString();
                
                if ( _lastFileName != null && ! es.contains( _lastFileName ) )
                    continue;
                
                int line = StringParseUtil.parseInt( es.substring( es.lastIndexOf( ":" ) + 1 ) , -1 );
                
                stack[i] = new StackTraceElement( getName() , stack[i].getMethodName() , getName() , getSourceLine( line ) );
                changed = true;
            }
            
            if ( ! changed )
                return;
            
            t.setStackTrace( stack );
        }
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
            return -1;
        
        int thisBlockStart = line;
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

    public String toString(){
        return getName();
    }
    
    private long _lastParse = 0;
    
    private List<Block> _blocks;
    private JSFunction _func;
    private JxpServlet _servlet;
    private Convert _convert;
    String _jsCode = null;

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

        InputStream getInputStream()
            throws IOException {
            return new FileInputStream( _f );
        }
        
        public long lastUpdated(){
            return _f.lastModified();
        }

        final File _f;
    }
}
