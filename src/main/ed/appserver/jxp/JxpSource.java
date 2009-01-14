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
import ed.lang.php.PHPJxpSource;
import ed.lang.ruby.RubyCGIAdapter;
import ed.lang.ruby.RubyJxpSource;
import ed.lang.ruby.RubyErbSource;
import ed.lang.python.PythonJxpSource;
import ed.util.*;
import ed.appserver.*;
import ed.appserver.adapter.cgi.SysExecCGIAdapter;
import ed.appserver.adapter.AdapterType;
import ed.appserver.templates.*;
import ed.appserver.templates.djang10.Djang10Source;

public abstract class JxpSource extends JSObjectLame implements Dependency , DependencyTracker {

    public static final String JXP_SOURCE_PROP = "_jxpSource";

    public static JxpSource getSource( File f , AppContext context , JSFileLibrary lib ){
        if ( f == null )
            throw new NullPointerException( "can't have null file" );

        JxpSource s = null;

        AdapterType adapterType = AdapterType.DIRECT_10GEN;

        if (context != null) {
            adapterType = context.getAdapterType(f);
        }

        if ( f.getName().endsWith(".djang10") ) {
            Scope parentScope = (context != null)? context.getScope() : Scope.getAScope();
            s = new Djang10Source(parentScope.child( "Djang10 Scope for: " + f ), f);
        }
        else if ( f.getName().endsWith( ".py" ) ) {
            s = Language.PYTHON().getAdapter(adapterType, f, context, lib);

            if (s == null) {
                s = new PythonJxpSource( f , lib );
            }
        }
        else if ( f.getName().endsWith( ".rb" ) ){
            s = Language.RUBY().getAdapter(adapterType, f, context, lib);

            if (s == null) {
                s = new RubyJxpSource(f);
            }
        }
        else if ( f.getName().endsWith( ".erb" ) || f.getName().endsWith( ".rhtml" ) )
            s = new RubyErbSource( f );

        else if ( f.getName().endsWith( ".php" ) )
            s = new PHPJxpSource( f );

        else if ( f.getName().endsWith( ".cgi" ) )
            s = new SysExecCGIAdapter( f );

        if( s == null )
            s = new JxpFileSource( f );
        s._lib = lib;
        s._context = context;
        return s;
    }

    // -----

    protected abstract String getContent() throws IOException;
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

        if ( ! ( t.getExtension().equals( "js" ) || t.getExtension().equals( "ssjs" ) ) )
            throw new RuntimeException( "don't know what to do with : " + t.getExtension() );

        try {
            Convert convert = new Convert( t.getName() , t.getContent() , (new CompileOptions()).sourceLanguage( t.getSourceLanguage()  ) );
            _func = convert.get();
            _func.set(JXP_SOURCE_PROP, this);
        if ( _lib != null )
        JSFileLibrary.addPath( _func , _lib );
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
            ProfilingTracker.tlGotTime( "compile" , System.currentTimeMillis() - start , 0 );
        }
    }


    private String _getFileSafeName(){
        return getName().replaceAll( "[^\\w]" , "_" );
    }

    public JxpServlet getServlet( AppContext context )
        throws IOException {
        _checkTime();
        JxpServlet temp = _servlet;
        if ( temp == null ){
            JSFunction f = getFunction();
            temp = new JxpServlet( context , f );
            JSFileLibrary.addPath( f.getClass() , _lib );
            _servlet = temp;
        }

        return temp;
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

    public Set<String> keySet( boolean includePrototype ){
        return new HashSet<String>();
    }

    public String toString(){
        return getName();
    }

    protected AppContext getAppContext(){
        return _context;
    }

    public long approxSize( SeenPath seen ){
        if ( _func == null )
            return 0;
        return _func.approxSize( seen );
    }

    protected long _lastParse = 0;
    protected List<Dependency> _dependencies = new ArrayList<Dependency>();

    private JSFunction _func;
    private JxpServlet _servlet;

    private JSFileLibrary _lib;
    private AppContext _context;

    // -------------------

    public static class JxpFileSource extends JxpSource implements Sizable {
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
