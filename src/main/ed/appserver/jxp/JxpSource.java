// Source.java

package ed.appserver.jxp;

import java.io.*;
import java.util.*;

import ed.io.*;
import ed.js.*;
import ed.js.engine.*;
import ed.lang.*;
import ed.util.*;
import ed.appserver.*;
import ed.appserver.templates.*;
import ed.appserver.templates.djang10.Djang10Source;

public abstract class JxpSource implements Dependency , DependencyTracker {
    
    public static final String JXP_SOURCE_PROP = "_jxpSource";

    static final File _tmpDir = new File( "/tmp/jxp/templates/" );
    static {
        _tmpDir.mkdirs();
    }

    public static JxpSource getSource( File f ){
        return getSource( f , null );
        
    }
    
    public static JxpSource getSource( File f , JSFileLibrary lib ){
        if ( f == null )
            throw new NullPointerException( "can't have null file" );
        
        if(f.getName().endsWith(".djang10"))
            return new Djang10Source(f);

        JxpSource s = new JxpFileSource( f );
        s._lib = lib;
        return s;
    }

    // -----

    abstract String getContent() throws IOException;
    abstract InputStream getInputStream() throws IOException ;
    public abstract long lastUpdated();
    abstract String getName();

    /**
     * @return File if it makes sense, otherwise nothing
     */
    public abstract File getFile(); 

    public void addDependency( Dependency d ){
        _dependencies.add( d );
    }

    public synchronized JSFunction getFunction()
        throws IOException {
        
        _checkTime();
        
        if ( _func != null )
            return _func;
        
        _lastParse = Calendar.getInstance().getTimeInMillis();
        _dependencies.clear();

        Template t = new Template( getName() , getContent() , Language.find( getName() ) );
        while ( ! t.getExtension().equals( "js" ) ){
            
            TemplateConverter.Result result = TemplateEngine.oneConvert( t , this );
            
            if ( result == null )
                break;
            
            if ( result.getLineMapping() != null )
                StackTraceHolder.getInstance().set( result.getNewTemplate().getName() , 
                                                    new BasicLineNumberMapper( t.getName() , result.getNewTemplate().getName() , result.getLineMapping() ) );
            
            t = result.getNewTemplate();
        }
        
        if ( ! t.getExtension().equals( "js" ) )
            throw new RuntimeException( "don't know what do do with : " + t.getExtension() );
        
        Convert convert = null;
        try {
            convert = new Convert( t.getName() , t.getContent() , false , t.getSourceLanguage() );
            _func = convert.get();
            _func.set(JXP_SOURCE_PROP, this);
            return _func;
        }
        catch ( Exception e ){
            String thing = e.toString();
            if ( thing.indexOf( ":" ) >= 0 )
                thing = thing.substring( thing.indexOf(":") + 1 );
            
            String msg = "couldn't compile ";
            if ( ! thing.contains( t.getName() ) )
                msg += " [" + t.getName() + "] ";
            msg += thing;

            throw new RuntimeException( msg , e );
        }
    }
    

    private String _getFileSafeName(){
        return getName().replaceAll( "[^\\w]" , "_" );
    }

    public JxpServlet getServlet( AppContext context )
        throws IOException {
        _checkTime();
        if ( _servlet == null ){
            JSFunction f = getFunction();
            _servlet = new JxpServlet( context , this , f );
            JSFileLibrary.addPath( f.getClass() , _lib );
        }
        return _servlet;
    }
    

    private void _checkTime(){
        if ( ! _needsParsing() )
            return;
        
        _func = null;
        _servlet = null;
    }

    protected boolean _needsParsing(){

        if ( _lastParse < lastUpdated() )
            return true;

        for ( Dependency d : _dependencies )
            if ( _lastParse < d.lastUpdated() )
                return true;

        return false;
    }

    public String toString(){
        return getName();
    }
    
    protected long _lastParse = 0;
    protected List<Dependency> _dependencies = new ArrayList<Dependency>();
    
    private JSFunction _func;
    private JxpServlet _servlet;

    private JSFileLibrary _lib;


    // -------------------
    
    public static class JxpFileSource extends JxpSource {
        protected JxpFileSource( File f ){
            _f = f;
        }
        
        String getName(){
            return _f.toString();
        }

        protected String getContent()
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
