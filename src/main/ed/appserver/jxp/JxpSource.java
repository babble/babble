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

public abstract class JxpSource {
    
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
            
            if ( result.getLineMapping() != null )
                StackTraceHolder.getInstance().set( result.getNewTemplate().getName() , 
                                                    new BasicLineNumberMapper( t.getName() , result.getNewTemplate().getName() , result.getLineMapping() ) );
            
            t = result.getNewTemplate();
        }
        
        if ( ! t.getExtension().equals( "js" ) )
            throw new RuntimeException( "don't know what do do with : " + t.getExtension() );
        
        try {
            Convert convert = new Convert( t.getName() , t.getContent() );
            _func = convert.get();
            return _func;
        }
        catch ( Exception e ){
            throw new RuntimeException( "couldn't compile : " + t.getName() , e );
        }
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
        
        _func = null;
        _servlet = null;
    }

    public String toString(){
        return getName();
    }
    
    private long _lastParse = 0;
    
    private JSFunction _func;
    private JxpServlet _servlet;


    // -------------------
    
    public static class JxpFileSource extends JxpSource {
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

        public File getFile(){
            return _f;
        }

        final File _f;
    }
}
