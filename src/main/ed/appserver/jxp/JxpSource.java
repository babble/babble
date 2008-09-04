// Source.java

/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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

public abstract class JxpSource extends JSObjectLame implements Dependency , DependencyTracker {
    
    public static final String JXP_SOURCE_PROP = "_jxpSource";

    public static JxpSource getSource( File f ){
        return getSource( f , null );
        
    }
    
    public static JxpSource getSource( File f , JSFileLibrary lib ){
        if ( f == null )
            throw new NullPointerException( "can't have null file" );
        
        if ( f.getName().endsWith(".djang10") )
            return new Djang10Source(f);
        
        if ( f.getName().endsWith( ".py" ) )
            return new ed.lang.python.PythonJxpSource( f , lib );
        
        if ( f.getName().endsWith( ".rb" ) )
            return new ed.lang.ruby.RubyJxpSource( f , lib );
        
        if ( f.getName().endsWith( ".erb" ) || f.getName().endsWith( ".rhtml" ) )
            return new ed.lang.ruby.RubyErbSource( f , lib );

        if ( f.getName().endsWith( ".php" ) )
            return new ed.lang.php.PHPJxpSource( f );

        JxpSource s = new JxpFileSource( f );
        s._lib = lib;
        return s;
    }

    // -----

    protected abstract String getContent() throws IOException;
    protected abstract InputStream getInputStream() throws IOException ;
    public abstract long lastUpdated(Set<Dependency> visitedDeps);
    public abstract String getName();

    //Convenience wrapper, override lastUpdated(Set<Dependency> visitedDeps) instead
    public final long lastUpdated() {
        return lastUpdated(new HashSet<Dependency>());
    }
    
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
        
        long start = System.currentTimeMillis();

        _lastParse = lastUpdated();
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
        catch ( JSCompileException jce ){
            throw jce;
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
        finally {
            ProfilingTracker.tlGotTime( "compile" , System.currentTimeMillis() - start );
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
            _servlet = new JxpServlet( context , f );
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

        return ( _lastParse < lastUpdated(new HashSet<Dependency>()) );
    }

    public Collection<String> keySet( boolean includePrototype ){
        return new LinkedList<String>();
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
        
        public String getName(){
            return _f.toString();
        }

        protected String getContent()
            throws IOException {
            return StreamUtil.readFully( _f , "utf8" );
        }

        protected InputStream getInputStream()
            throws IOException {
            return new FileInputStream( _f );
        }
        
        public long lastUpdated(Set<Dependency> visitedDeps){
            visitedDeps.add(this);

            long lastUpdated = _f.lastModified();
            for(Dependency dep : _dependencies)
                if(!visitedDeps.contains(dep))
                    lastUpdated = Math.max(lastUpdated, dep.lastUpdated(visitedDeps));
            
            return lastUpdated;
        }

        public File getFile(){
            return _f;
        }

        final File _f;
    }
}
