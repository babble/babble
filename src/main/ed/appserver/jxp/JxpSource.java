// Source.java

package ed.appserver.jxp;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import ed.io.*;
import ed.util.*;
import ed.js.*;
import ed.js.engine.*;
import ed.lang.*;
import ed.appserver.*;
import ed.appserver.templates.*;

public abstract class JxpSource implements StackTraceFixer {
    
    static enum Language { JS , RUBY };

    static final File _tmpDir = new File( "/tmp/jxp/templates/" );
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

    boolean isTemplate(){
        final String name = getName();
        return 
            name.endsWith( ".html" ) || 
            name.endsWith( ".rhtml" );
    }
    
    Language getLanguage(){
        final String name = getName();
        if ( name.endsWith( ".rb" ) || 
             name.endsWith( ".rhtml" ) )
            return Language.RUBY;
        return Language.JS;
    }

    synchronized List<Block> getBlocks()
        throws IOException {
        _checkTime();
        if ( _blocks == null ){
            _lastParse = lastUpdated();
            _blocks = Parser.parse( this );
        }
        return _blocks;
    }

    public synchronized JSFunction getFunction1()
        throws IOException {
        
        _checkTime();
        
        if ( _func == null ){
            File jsFile = null;
            String extension = MimeTypes.getExtension( getName() );
            try {
                
                if ( extension.equals( "js" ) 
                     || extension.equals( "jxp" )
                     || extension.equals( "rhtml" )
                     || extension.equals( "html" )
                     ){
                    
                    Generator g = Generator.genJavaScript( getBlocks() );
                    _jsCodeToLines = g._jsCodeToLines;
                    _jsCode = g.toString();
                    
                    if ( ! getName().endsWith( ".js" ) )
                        _jsCode += "\n print( \"\\n\" );";

                    if ( extension.equals( "rhtml" ) ){
                        ed.lang.ruby.RubyConvert rc = new ed.lang.ruby.RubyConvert( getName() , _jsCode );
                        _jsCode = rc.getJSSource();
                    }

                }
                else if ( extension.equals( "rb" ) ){
                    ed.lang.ruby.RubyConvert rc = new ed.lang.ruby.RubyConvert( getName() , getInputStream() );
                    _jsCode = rc.getJSSource();
                }
                else {
                    throw new RuntimeException( "unkown extension [" + extension + "]" );
                }
                
                jsFile = new File( _tmpDir , _getFileSafeName() + ".js" );
                _lastFileName = jsFile.getName();
                
                FileOutputStream fout = new FileOutputStream( jsFile );
                fout.write( _jsCode.getBytes() );
                fout.close();
                
                try {
                    _convert = new Convert( jsFile );
                    _func = _convert.get();
                    StackTraceHolder.getInstance().set( jsFile.getAbsolutePath() , this );
                }
                catch ( Exception e ){
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

    public synchronized JSFunction getFunction()
        throws IOException {
        
        _checkTime();
        
        if ( _func != null )
            return _func;
        
        _lastParse = lastUpdated();

        Template t = new Template( getName() , getContent() );
        while ( ! t.getExtension().equals( "js" ) ){
            
            TemplateConverter.Result result = TemplateEngine.oneConvert( t );
            
            if ( result == null )
                break;
            
            StackTraceHolder.getInstance().set( result.getNewTemplate().getName() , 
                                                new BasicLineNumberMapper( t.getName() , result.getNewTemplate().getName() , result.getLineMapping() ) );
            
            t = result.getNewTemplate();
        }
        
        if ( ! t.getExtension().equals( "js" ) )
            throw new RuntimeException( "don't know what do do with : " + t.getExtension() );

        Convert convert = new Convert( t.getName() , t.getContent() );
        _func = convert.get();
        return _func;
    }
    

    private String _getFileSafeName(){
        return getName().replaceAll( "[^\\w]" , "_" );
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

    public StackTraceElement fixSTElement( StackTraceElement element ){
        if ( _jsCodeToLines == null )
            return null;

        String es = element.toString();
        
        if ( _lastFileName != null && ! es.contains( _lastFileName ) )
            return null;
        
        int line = element.getLineNumber();
        return new StackTraceElement( getName() , element.getMethodName() , getName() , getSourceLine( line ) );
    }

    public boolean removeSTElement( StackTraceElement element ){
        return false;
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
