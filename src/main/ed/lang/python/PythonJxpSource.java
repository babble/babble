// PythonJxpSource.java

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

package ed.lang.python;

import java.io.*;
import java.util.*;

import org.python.core.*;
import org.python.expose.*;
import org.python.Version;

import ed.js.*;
import ed.log.*;
import ed.js.engine.*;
import ed.lang.*;
import ed.util.*;
import ed.appserver.*;
import ed.appserver.jxp.*;

public class PythonJxpSource extends JxpSource {

    static {
        System.setProperty( "python.cachedir", ed.io.WorkingFiles.TMP_DIR + "/jython-cache/" + Version.PY_VERSION );
    }

    public PythonJxpSource( File f , JSFileLibrary lib ){
        _file = f;
        _lib = lib;
    }
    
    protected String getContent(){
        throw new RuntimeException( "you can't do this" );
    }
       
    protected InputStream getInputStream(){
        throw new RuntimeException( "you can't do this" );
    }
    
    public long lastUpdated(Set<Dependency> visitedDeps){
        return _file.lastModified();
    }
    
    public String getName(){
        return _file.toString();
    }

    public File getFile(){
        return _file;
    }

    static class MyStdoutFile extends PyFile {
        MyStdoutFile(AppRequest request){
            _request = request;
        }
        public void flush(){}

        public void write( String s ){
            if( _request == null )
                // Log
                _log.info( s );
            else
                _request.print( s );
        }
        AppRequest _request;
    }

    public synchronized JSFunction getFunction()
        throws IOException {
        
        final PyCode code = _getCode();
        
        return new ed.js.func.JSFunctionCalls0(){
            public Object call( Scope s , Object extra[] ){
                
                PyObject args[] = new PyObject[ extra == null ? 0 : extra.length ];
                for ( int i=0; i<args.length; i++ )
                    args[i] = Python.toPython( extra[i] );
                
                final AppRequest ar = AppRequest.getThreadLocal();
                
                PySystemState ss = Py.getSystemState();

                // Have to flush every module that depends on any module that
                // has been updated
                if( ! ( ss.modules instanceof PythonModuleTracker ) ){
                    if( ss.modules instanceof PyStringMap)
                        ss.modules = new PythonModuleTracker( (PyStringMap)ss.modules );
                    else {
                        // You can comment out this exception, it shouldn't
                        // break anything beyond reloading python modules
                        throw new RuntimeException("couldn't intercept modules " + ss.modules.getClass());
                    }
                }
                // Current approach: when a file gets updated, flush all the
                // site's modules.
                // This sucks a little, but I think it'd be even harder to
                // figure out which modules depend on which modules and flush
                // only the right ones.
                // FIXME: This won't work if I import a file from core.modules
                // or local.modules and then edit that file.
                // This'll be a problem for developers, but right now I wanna
                // try and get a fix to the ShopWiki guys.
                else {
                    PythonModuleTracker mods = (PythonModuleTracker)ss.modules;
                    AppContext ctxt = (AppContext)s.get( "__instance__" );
                    if ( ctxt != null )
                        mods.flushOld( ctxt.getRoot() );
                }
                


                PyObject out = ss.stdout;
                if ( ! ( out instanceof MyStdoutFile ) || ((MyStdoutFile)out)._request != ar ){
                    ss.stdout = new MyStdoutFile( ar );
                }
                
                addPath( ss , _lib.getRoot().toString() );
                addPath( ss , _lib.getTopParent().getRoot().toString() );

                Scope pyglobals = s.child( "scope to hold python builtins" );

                PyObject globals = new PyJSScopeWrapper( pyglobals , false );
                Scope tl = pyglobals.getTLPreferred();

                pyglobals.setGlobal( true );
                __builtin__.fillWithBuiltins( globals );
                globals.invoke( "update", PySystemState.builtins );

                PyObject builtins = ss.builtins;

                PyObject pyImport = builtins.__finditem__( "__import__" );
                if( ! ( pyImport instanceof TrackImport ) )
                    builtins.__setitem__( "__import__" , new TrackImport( pyImport , (PythonModuleTracker)ss.modules ) );

                pyglobals.setGlobal( false );

                PyModule xgenMod = imp.addModule("_10gen");
                // I know this is appalling but they don't expose this any other
                // way
                xgenMod.__dict__ = globals;

                //Py.initClassExceptions( globals );
                globals.__setitem__( "__file__", Py.newString( _file.toString() ) );
                PyModule module = new PyModule( "main" , globals );

                PyObject locals = module.__dict__;

                return Py.runCode( code, locals, globals );
            }
        };
    }

    static void addPath( PySystemState ss , String myPath ){

        for ( Object o : ss.path )
            if ( o.toString().equals( myPath ) )
                return;
        
        ss.path.append( Py.newString( myPath ) );
    }

    private PyCode _getCode()
        throws IOException {
        PyCode c = _code;
	final long lastModified = _file.lastModified();
        if ( c == null || _lastCompile < lastModified ){
            c = Python.compile( _file );
            _code = c;
            _lastCompile = lastModified;
        }
        return c;
    }

    final File _file;
    final JSFileLibrary _lib;

    private PyCode _code;
    private long _lastCompile;
    
    // static b/c it has to use ThreadLocal anyway
    final static Logger _log = Logger.getLogger( "python" );
}

class TrackImport extends PyObject {
    PyObject _import;
    PythonModuleTracker _moduleDict;
    TrackImport( PyObject importF , PythonModuleTracker sys_modules ){
        _import = importF;
        _moduleDict = sys_modules;
    }

    public PyObject __call__( PyObject args[] , String keywords[] ){
        int argc = args.length;
        PyObject globals = ( argc > 1 ) ? args[1] : null;
        //System.out.println("Overrode import importing. " + args[0] + " " + globals.__finditem__( "__file__" ) );
        PyObject m = _import.__call__( args, keywords );
        return m;

        //PythonJxpSource foo = PythonJxpSource.this;
    }
}
