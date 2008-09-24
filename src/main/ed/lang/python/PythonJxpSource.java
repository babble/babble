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

    public synchronized JSFunction getFunction()
        throws IOException {
        
        final PyCode code = _getCode();
        
        return new ed.js.func.JSFunctionCalls0(){
            public Object call( Scope s , Object extra[] ){

                PyObject args[] = new PyObject[ extra == null ? 0 : extra.length ];
                for ( int i=0; i<args.length; i++ )
                    args[i] = Python.toPython( extra[i] );
                
                final AppRequest ar = AppRequest.getThreadLocal();
                final AppContext ac = getAppContext();

                SiteSystemState ss = Python.getSiteSystemState( ac , s );
                PySystemState pyOld = Py.getSystemState();

                ss.flushOld();

                ss.setOutput( ar );

                addPath( ss.getPyState() , _lib.getRoot().toString() );
                addPath( ss.getPyState() , _lib.getTopParent().getRoot().toString() );

                PyObject globals = ss.globals;
                // Careful -- this is static PySystemState.builtins
                PyObject builtins = ss.getPyState().builtins;

                PyObject pyImport = builtins.__finditem__( "__import__" );
                if( ! ( pyImport instanceof TrackImport ) )
                    builtins.__setitem__( "__import__" , new TrackImport( pyImport ) );

                try {
                    Py.setSystemState( ss.getPyState() );


                    //Py.initClassExceptions( globals );
                    globals.__setitem__( "__file__", Py.newString( _file.toString() ) );
                    PyModule module = new PyModule( "__main__" , globals );

                    PyObject locals = module.__dict__;
                    return Py.runCode( code, locals, globals );
                }
                finally {
                    Py.setSystemState( pyOld );
                }
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
    
    void addDependency( String to ){
        super.addDependency( new FileDependency( new File( to ) ) );
    }

    class TrackImport extends PyObject {
        PyObject _import;
        TrackImport( PyObject importF ){
            _import = importF;
        }

        public PyObject __call__( PyObject args[] , String keywords[] ){
            SiteSystemState sss = Python.getSiteSystemState( null , Scope.getThreadLocal() );
            
            int argc = args.length;
            // Second argument is the dict of globals. Mostly this is helpful
            // for getting context -- file or module *doing* the import.
            PyObject globals = ( argc > 1 ) ? args[1] : null;

            //System.out.println("Overrode import importing. import " + args[0] + " in file " + globals.__finditem__( "__file__" ) );

            PyObject m = _import.__call__( args, keywords );

            if( globals == null ){
                // Only happens (AFAICT) from within Java code.
                // For example, Jython's codecs.java calls
                // __builtin__.__import__("encodings");
                return m;
            }


            // gets the module name -- __file__ is the file
            PyObject importer = globals.__finditem__( "__name__".intern() );

            PyObject to = m.__findattr__( "__name__".intern() );
            // no __file__: builtin or something -- don't bother adding
            // dependency
            if( to == null ) return m;

            // Add a plain old JXP dependency on the file that was imported
            // Not sure if this is helpful or not
            // Can't do this right now -- one TrackImport is created for all
            // PythonJxpSources. FIXME.
            //addDependency( to.toString() );

            // Add a module dependency -- module being imported was imported by
            // the importing module
            sss.addDependency( to , importer );
            return m;

            //PythonJxpSource foo = PythonJxpSource.this;
        }
    }

    // static b/c it has to use ThreadLocal anyway
    final static Logger _log = Logger.getLogger( "python" );
}
