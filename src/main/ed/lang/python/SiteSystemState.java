// SiteSystemState.java

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

import org.python.expose.*;
import org.python.Version;
import org.python.expose.generate.*;
import java.io.*;
import java.util.*;

import org.python.core.*;

import ed.appserver.*;
import ed.log.*;
import ed.js.*;
import ed.log.*;
import ed.js.engine.*;
import ed.lang.*;
import ed.util.*;
import ed.appserver.jxp.*;

/**
 * 10gen-specific Python system state.
 *
 * Originally this was going to be a subclass of PySystemState, but
 * this lead to exciting breakage in calls to sys.getEnviron(). It
 * seems that Python introspects for method calls on whatever object
 * is used as Py.getSystemState(), and this introspection isn't very
 * smart -- specifically, it doesn't pick up on methods inherited from
 * a superclass. As a result, sys.getEnviron() can't be found and
 * everything breaks. This even happens in modules like os.
 *
 * This is our new approach. Instead of re-wrapping all those method
 * calls, we just store a PySystemState and hopefully do all the
 * 10gen-specific munging here. Our caller should pass
 * SiteSystemState.getPyState() to Py.setSystemState as needed.
 */
public class SiteSystemState {
    static final boolean DEBUG = Boolean.getBoolean( "DEBUG.SITESYSTEMSTATE" );
    
    SiteSystemState( AppContext ac , PyObject newGlobals , Scope s){
        pyState = new PySystemState();
        globals = newGlobals;
        _scope = s;
        _context = ac;
        setupModules();
        ensureMetaPathHook( pyState , s );

        // Careful -- this is static PySystemState.builtins. We modify
        // it once to intercept imports for all sites. TrackImport
        // then looks at the execution environment and figures out which site
        // needs to track the import.
        PyObject builtins = PySystemState.builtins;
        PyObject pyImport = builtins.__finditem__( "__import__" );
        if( ! ( pyImport instanceof TrackImport ) )
            builtins.__setitem__( "__import__" , new TrackImport( pyImport ) );
    }

    public PySystemState getPyState(){
        return pyState;
    }

    /**
     * Set up module interception code.
     *
     * We replace sys.modules with a subclass of PyDictionary so we
     * can intercept calls to import and flush old versions of modules
     * when needed.
     */
    public void setupModules(){
        if( ! ( pyState.modules instanceof PythonModuleTracker ) ){
            if( pyState.modules instanceof PyStringMap)
                pyState.modules = new PythonModuleTracker( (PyStringMap)pyState.modules );
            else {
                // You can comment out this exception, it shouldn't
                // break anything beyond reloading python modules
                throw new RuntimeException("couldn't intercept modules " + pyState.modules.getClass());
            }
        }

        String modName = "_10gen".intern();
        if( pyState.modules.__finditem__( modName ) == null ){
            PyModule xgenMod = new PyModule( modName );
            pyState.modules.__setitem__( modName , xgenMod );
            // I know this is appalling but they don't expose this any other
            // way
            xgenMod.__dict__ = globals;
        }

        // This allows you to do import sitename.foo
        // importing sitename.foo will rerun your foo.py or whatever,
        // ends up as a different module from just importing foo. This also
        // means it'll get dependency-tracked separately -- but it will get
        // tracked.
        modName = _context.getName().intern();
        if( pyState.modules.__finditem__( modName ) == null ){
            PyModule siteMod = new PyModule( modName );
            pyState.modules.__setitem__( modName , siteMod );
            PyList __path__ = new PyList( PyString.TYPE );
            __path__.append( new PyString( _context.toString() ) );
            siteMod.__dict__.__setitem__( "__path__", __path__ );
        }

    }

    private void _checkModules(){
        if( ! ( pyState.modules instanceof PythonModuleTracker ) ){
            throw new RuntimeException( "i'm not sufficiently set up yet" );
        }
    }

    /**
     * Flush old modules that have been imported by Python code but
     * whose source is now newer.
     */
    public Set<File> flushOld(){
        return ((PythonModuleTracker)pyState.modules).flushOld();
    }

    private void ensureMetaPathHook( PySystemState ss , Scope scope ){
        boolean foundMetaPath = false;
        for( Object m : ss.meta_path ){
            if( ! ( m instanceof PyObject ) ) continue; // ??
            PyObject p = (PyObject)m;
            if( p instanceof ModuleFinder )
                return;
        }
        
        ss.meta_path.append( new ModuleFinder( scope ) );
    }


    public void addDependency( PyObject to, PyObject importer ){
        _checkModules();
        ((PythonModuleTracker)pyState.modules).addDependency( to , importer );
    }

    /**
     * Set output to an AppRequest.
     *
     * Replace the Python sys.stdout with a file-like object which
     * actually prints to an AppRequest stream.
     */
    public void setOutput( AppRequest ar ){
        PyObject out = pyState.stdout;
        if ( ! ( out instanceof MyStdoutFile ) || ((MyStdoutFile)out)._request != ar ){
            pyState.stdout = new MyStdoutFile( ar );
        }
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


    class TrackImport extends PyObject {
        PyObject _import;
        TrackImport( PyObject importF ){
            _import = importF;
        }

        public PyObject __call__( PyObject args[] , String keywords[] ){
            int argc = args.length;
            // Second argument is the dict of globals. Mostly this is helpful
            // for getting context -- file or module *doing* the import.
            PyObject globals = ( argc > 1 ) ? args[1] : null;

            if( DEBUG ){
                System.out.println("Overrode import importing. import " + args[0]);
                System.out.println("globals? " + (globals == null ? "null" : "not null, file " + globals.__finditem__("__file__")));
            }

            PyObject m = _import.__call__( args, keywords );

            if( globals == null ){
                // Only happens (AFAICT) from within Java code.
                // For example, Jython's codecs.java calls
                // __builtin__.__import__("encodings");
                // Python calls to __import__ provide an empty Python dict.
                return m;
            }


            // gets the module name -- __file__ is the file
            PyObject importer = globals.__finditem__( "__name__".intern() );
            if( importer == null ){
                // Globals was empty? Maybe we were called "manually" with
                // __import__, or maybe import is happening from an exec()
                // or something.
                // Let's try to get the place that called the import manually
                // and hope for the best.
                PyFrame f = Py.getFrame();
                if( f == null ){
                    // No idea what this means
                    System.err.println("Can't figure out where the call to import " + args[0] + " came from! Import tracking is going to be screwed up!");
                    return m;
                }

                globals = f.f_globals;
                importer = globals.__finditem__( "__name__".intern() );
                if( importer == null ){
                    // Probably an import from within an exec("foo", {}).
                    // Let's go for broke and try to get the filename from
                    // the PyFrame. This won't be tracked any further,
                    // but that's fine -- at least we'll know which file
                    // needs to be re-exec'ed (e.g. for modjy).
                    // FIXME: exec('import foo', {}) ???
                    //   -- filename is <string> or what?
                    PyTableCode code = f.f_code;
                    // FIXME: wrap just to unwrap later
                    importer = new PyString( code.co_filename );
                }

                if( importer == null ){ // Still??
                    System.err.println("Totally unable to figure out how import to " + args[0] + " came about. Import tracking is going to be screwed up.");
                }
            }

            // We have to return m, but that might not be the module itself.
            // If we got "import foo.bar", m = foo, but we want to get 
            // bar.__name__. So we have to search through modules to get to the
            // innermost.
            // But if we got "from foo import bar", m = bar, and we don't want
            // to do anything. Ahh, crappy __import__ semantics..
            // For more information see http://docs.python.org/lib/built-in-funcs.html
            PyObject fromlist = (argc > 3) ? args[3] : null;
            PyObject innerMod = null;
            if( fromlist != null && fromlist.__len__() > 0 ) innerMod = m;
            else {
                innerMod = m;
                PyObject targetP = args[0];
                if( targetP instanceof PyString ){
                    String target = targetP.toString();
                    String [] modNames = target.split("\\.");

                    for( int i = 1; i < modNames.length; ++i ){
                        innerMod = innerMod.__findattr__( modNames[i].intern() );
                    }
                }
                else {
                    // ?? 
                    // Someone hates us..
                    System.err.println("I will not be party to this madness " + targetP.getClass());
                }
            }

            PyObject to = innerMod.__findattr__( "__name__".intern() );
            if( to == null ) return m;

            // Add a plain old JXP dependency on the file that was imported
            // Not sure if this is helpful or not
            // Can't do this right now -- one TrackImport is created for all
            // PythonJxpSources. FIXME.
            //addDependency( to.toString() );

            // Add a module dependency -- module being imported was imported by
            // the importing module
            SiteSystemState sss = Python.getSiteSystemState( null , Scope.getThreadLocal() );
            sss.addDependency( to , importer );
            return m;

            //PythonJxpSource foo = PythonJxpSource.this;
        }
    }


    /**
     * sys.meta_path hook to deal with core/core-modules and local/local-modules
     * imports.
     *
     * Python meta_path hooks are one of many ways a program can
     * customize how/where modules are loaded. They have two parts,
     * finders and loaders. This is the finder class, whose API
     * consists of one method, find_module.
     *
     * For more details on the meta_path hooks, check PEP 302.
     */
    @ExposedType(name="_10gen_module_finder")
    public class ModuleFinder extends PyObject {
        Scope _scope;
        JSLibrary _coreModules;
        JSLibrary _core;
        JSLibrary _local;
        JSLibrary _localModules;
        ModuleFinder( Scope s ){
            _scope = s;
            Object core = s.get( "core" );
            if( core instanceof JSLibrary )
                _core = (JSLibrary)core;
            if( core instanceof JSObject ){
                Object coreModules = ((JSObject)core).get( "modules" );
                if( coreModules instanceof JSLibrary )
                    _coreModules = (JSLibrary)coreModules;
            }

            Object local = s.get( "local" );
            if( local instanceof JSLibrary )
                _local = (JSLibrary)local;
            if( local instanceof JSObject ){
                Object localModules = ((JSObject)local).get( "modules" );
                if( localModules instanceof JSLibrary )
                    _localModules = (JSLibrary)localModules;
            }
        }

        /**
         * The sole interface to a finder. We create virtual modules
         * for "core" and "core.modules", and any relative import
         * within a core module has to be handled specially.
         * Specifically, an import for baz from within
         * core.modules.foo.bar comes out as an import for
         * core.modules.foo.bar.baz (with __path__ =
         * ['/data/core-modules/foo/bar']) and if we can't find it, we
         * try core.modules.foo.baz (simulating core.modules.foo being
         * on the module search path).
         *
         * Alternately, we could just add core.modules.foo to sys.path
         * when it gets imported, but Geir says we should make it like
         * JS, which means ugly and painful.
         *
         * find_module returns a "loader", as specified by PEP 302.
         *
         * @param fullname {PyString} name of the module to find
         * @param path {PyList} optional; the __path__ of the module
         */
        @ExposedMethod(names={"find_module"})
        public PyObject find_module( PyObject args[] , String keywords[] ){
            int argc = args.length;
            assert argc >= 1;
            assert args[0] instanceof PyString;
            String modName = args[0].toString();
            if( modName.equals("core.modules") ){
                return new LibraryModuleLoader( _coreModules );
            }

            if( modName.startsWith("core.modules.") ){
                // look for core.modules.foo.bar...baz
                // and try core.modules.foo.baz
                // Should confirm that this is from within core.modules.foo.bar... using __path__
                int period = modName.indexOf('.') + 1; // core.
                period = modName.indexOf( '.' , period ) + 1; // modules.
                int next = modName.indexOf( '.' , period ); // foo
                if( next != -1 && modName.indexOf( '.' , next + 1 ) != -1 ){
                    String foo = modName.substring( period , next );
                    File fooF = new File( _coreModules.getRoot() , foo );
                    String baz = modName.substring( modName.lastIndexOf( '.' ) + 1 );
                    File bazF = new File( fooF , baz );
                    File bazPyF = new File( fooF , baz + ".py" );
                    if( bazF.exists() || bazPyF.exists() ){
                        return new RewriteModuleLoader( modName.substring( 0 , next ) + "." + baz );
                    }
                }
            }

            if( modName.equals("core") ){
                return new LibraryModuleLoader( _core );
            }

            return Py.None;
        }
    }

    /**
     * A module loader for core, core.modules, etc.
     *
     * Basically this wraps a JSLibrary in such a way that when the
     * module is loaded, sub-modules can be found by the default
     * Python search.  (Specifically we set the __path__ to the root
     * of the library.) This obviates the need for putting __init__.py
     * files throughout corejs and core-modules.
     */
    @ExposedType(name="_10gen_module_library_loader")
    public class LibraryModuleLoader extends PyObject {
        JSLibrary _root;
        LibraryModuleLoader( Object start ){
            assert start instanceof JSLibrary;
            _root = (JSLibrary)start;
        }

        public JSLibrary getRoot(){
            return _root;
        }


        /**
         * The load_module method specified in PEP 302.
         *
         * @param fullname {PyString} the full name of the module
         * @return PyModule
         */
        @ExposedMethod(names={"load_module"})
        public PyModule load_module( String name ){
            PyModule mod = imp.addModule( name );
            PyObject __path__ = mod.__findattr__( "__path__".intern() );
            if( __path__ != null ) return mod; // previously imported

            mod.__setattr__( "__file__".intern() , new PyString( "<10gen_virtual>" ) );
            mod.__setattr__( "__loader__".intern() , this );
            PyList pathL = new PyList( PyString.TYPE );
            pathL.append( new PyString( _root.getRoot().toString() ) );
            mod.__setattr__( "__path__".intern() , pathL );

            return mod;
        }
    }

    /**
     * Module loader that loads a module different than specified.
     * We use this when a core-module imports a file that exists at the top
     * of the core-module. This way, core-modules can pretend they're on
     * sys.path, but without actually being sys.path. (Don't want life to be
     * too easy for people on our platform.)
     */
    @ExposedType(name="_10gen_module_rewrite_loader")
    public class RewriteModuleLoader extends PyObject {
        String _realName;
        RewriteModuleLoader( String real ){
            _realName = real;
        }

        /**
         * The load_module method specified in PEP 302.
         *
         * @param fullname {PyString} the full name of the module
         * @return PyModule
         */
        @ExposedMethod(names={"load_module"})
        public PyObject load_module( String name ){
            PyObject m = __builtin__.__import__(_realName);
            String components = _realName.substring( _realName.indexOf('.') + 1 );
            while( components.indexOf('.') != -1 ){
                String component = components.substring( 0 , components.indexOf('.') );
                m = m.__findattr__( component.intern() );
                components = components.substring( components.indexOf('.') + 1 );
            }
            m = m.__findattr__( components.intern() );
            return m;
        }
    }

    final static Logger _log = Logger.getLogger( "python" );
    final public PyObject globals;
    private PySystemState pyState;
    private AppContext _context;
    private Scope _scope;
}
