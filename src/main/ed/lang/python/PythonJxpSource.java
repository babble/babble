// PythonJxpSource.java

/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

package ed.lang.python;

import java.io.*;
import java.util.*;

import org.python.core.*;
import org.python.Version;

import ed.js.*;
import ed.js.engine.*;
import ed.util.*;
import ed.appserver.*;
import ed.appserver.jxp.JxpSource;
import ed.log.Logger;

public class PythonJxpSource extends JxpSource implements Sizable {

    static {
        System.setProperty("python.cachedir", ed.io.WorkingFiles.getTypeDir( "jython-cache" ) + Version.PY_VERSION);
    }

    public synchronized JSFunction getFunction()
        throws IOException {

        final PyCode code = _getCode();

        return new ed.js.func.JSFunctionCalls0(){
            public Object call( Scope s , Object extra[] ){

                final AppContext ac = getAppContext();

                Scope siteScope;

                if ( ac != null )
                    siteScope = ac.getScope();
                else
                    siteScope = s.getGlobal( true );

                SiteSystemState ss = Python.getSiteSystemState( ac , siteScope );

                PyObject globals = new PyDictionary();

                PyObject result = runPythonCode(code, ac, ss, globals, _lib, _file);

                if (usePassedInScope()){
                    PyObject keys = globals.invoke("keys");
                    if( ! ( keys instanceof PyList ) ){
                        throw new RuntimeException("couldn't use passed in scope: keys not dictionary [" + keys.getClass() + "]");
                    }

                    PyList keysL = (PyList)keys;
                    for(int i = 0; i < keysL.size(); i++){
                        PyObject key = keysL.pyget(i);
                        if( ! ( key instanceof PyString ) ){
                            System.out.println("Non-string key in globals : " + key + " [skipping]");
                            continue;
                        }

                        s.put( key.toString(), Python.toJS( globals.__finditem__(key) ) , true );
                    }
                }
                return Python.toJS( result );
            }
        };
    }

    /**
     * Provides a sensible environment to run Python code. This includes
     * providing a globals dictionary, a module to run in (for import tracking),
     * setting __name__ and __file__, making sure the path includes some
     * important directories, etc.
     */
    public static PyObject runPythonCode(PyCode code, AppContext ac, SiteSystemState ss, PyObject globals,
                                         JSFileLibrary lib, File file) {
        return runPythonCode(code, ac, ss, globals, lib, file, false);
    }

    public static PyObject runPythonCode(PyCode code, AppContext ac, SiteSystemState ss, PyObject globals,
                                         JSFileLibrary lib, File file, boolean main) {

        PySystemState pyOld = Py.getSystemState();

        ss.flushOld();

        // Running this file should not contaminate the sys.path outside of its
        // runtime. This isn't the best solution..
        ss.addPath( file.getParent() );
        ss.ensurePath( lib.getRoot().getAbsolutePath() );
        ss.ensurePath( lib.getTopParent().getRoot().getAbsolutePath() );

        PyObject oldMain = ss.getPyState().modules.__finditem__( Py.newString( _MAIN ) );
        PyObject result = null;

        try {
            Py.setSystemState( ss.getPyState() );

            globals.__setitem__( "__file__", Py.newString( file.getAbsolutePath() ) );
            try {
                String canonical = file.getCanonicalPath();
                if( ac != null ){
                    String root = ac.getFile( "." ).getCanonicalPath();
                    if( canonical.startsWith( root ) ){
                        String relative = canonical.substring( root.length() + 1 );
                        globals.__setitem__( "__file__", Py.newString( relative.toString() ) );
                    }
                }
            }
            catch(IOException e){
                /* File.getCanonicalPath() throws IOException! Really!
                   We already set a reasonable __file__, so whatever */
            }

            /*
             * In order to track dependencies, we need to know what module is doing imports
             * We just care that _init doing imports is registered at all.
             */
            PyModule module = new PyModule( _MAIN, globals );

            ss.getPyState().modules.__setitem__( Py.newString(_MAIN) , module );

            PyObject locals = module.__dict__; // FIXME: locals == globals ?
            result = Py.runCode( code, locals, globals );
            if( file.toString().endsWith( "_init.py" ) )
                ss.addRecursive(_MAIN, ac );
        }
        finally {
            Py.setSystemState( pyOld );
            ss.removePath( file.getParent() );
            ss.getPyState().modules.__setitem__( Py.newString(_MAIN) , oldMain );
        }
        return result;
    }

    private static void _globalRestore( PyObject globals , String name , PyObject value ){
        if( value != null ){
            globals.__setitem__( name , value  );
        }
        else{
            globals.__delitem__( name );
        }
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

    void addDependency( String to ){
        super.addDependency( new FileDependency( new File( to ) ) );
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

    public long approxSize( SeenPath seen ){
        return // super.approxSize( seen ) +
            JSObjectSize.size( _file , seen , this ) +
            JSObjectSize.size( _lib , seen , this ) +
            JSObjectSize.size( _code , seen ,this ) +
            JSObjectSize.size( _lastCompile , seen , this ) +
            JSObjectSize.size( _log , seen , this );
    }
    // static b/c it has to use ThreadLocal anyway
    final static Logger _log = Logger.getLogger( "python" );

    private static final String _MAIN = "__main__";
}
