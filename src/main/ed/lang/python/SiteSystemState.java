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
import java.io.*;
import java.util.*;

import org.python.core.*;

import ed.appserver.*;
import ed.log.*;
import ed.js.*;
import ed.js.engine.*;
import ed.util.*;

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
public class SiteSystemState implements Sizable {
    static final boolean DEBUG = Boolean.getBoolean( "DEBUG.SITESYSTEMSTATE" );
    static final String COREMODULES_STRING = "COREMODULES_10GEN";
    static final String CORE_MODULES_MARKER = "I AM IN A CORE MODULE";

    SiteSystemState( AppContext ac , PyObject newGlobals , Scope s){
        pyState = new PySystemState();
        globals = newGlobals;
        _scope = s;
        _context = ac;
        setupModules();
        ensureMetaPathHook( pyState , s );
        ensurePathHook( pyState , s );
        ensurePath( COREMODULES_STRING );
        replaceOutput();
        ensurePath( Config.get().getProperty("ED_HOME", "/data/ed")+"/src/main/ed/lang/python/lib" , 0 );

        // Careful -- this is static PySystemState.builtins. We modify
        // it once to intercept imports for all sites. TrackImport
        // then looks at the execution environment and figures out which site
        // needs to track the import.
        PyObject builtins = PySystemState.builtins;
        PyObject pyImport = builtins.__finditem__( "__import__" );
        if( ! ( pyImport instanceof TrackImport ) )
            builtins.__setitem__( "__import__" , new TrackImport( pyImport ) );

        if( ac != null ){
            try {
                /*
                 * NOTE : chdir!
                 *
                 * We do this because I didn't want to hack open() to find the
                 * right location to put a file in.
                 * Long term it should be eliminated.
                 */
                pyState.setCurrentWorkingDir( ac.getFile(".").getAbsolutePath() );
            }
            catch(FileNotFoundException e){
                throw new RuntimeException("Error : can't find '.' for app contextto set CWD in python");
            }
        }
    }

    public PySystemState getPyState(){
        return pyState;
    }

    void ensurePath( String myPath ){
        ensurePath( myPath , 0 );
    }

    void ensurePath( String myPath , int location ){

        for ( Object o : pyState.path )
            if ( o.toString().equals( myPath ) )
                return;

        if( location == -1 )
            pyState.path.append( Py.newString( myPath ) );
        else
            pyState.path.insert( location , Py.newString( myPath ) );
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
            PyModule xgenMod = new PyModule( modName , globals );
            pyState.modules.__setitem__( modName , xgenMod );
        }

        // This allows you to do import sitename
        // We handle import sitename.foo in the __import__ handler.
        if( _context != null ){
            modName = _context.getName().intern();
            if( pyState.modules.__finditem__( modName ) == null ){
                PyModule siteMod = new PyModule( modName );
                pyState.modules.__setitem__( modName , siteMod );
                // Make sure it's not a real package module that can
                // actually cause imports. This should only happen virtually.
                siteMod.__dict__.__setitem__( "__path__", Py.None );
            }
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

    private void ensurePathHook( PySystemState ss , Scope scope ){
        boolean foundHook = false;
        for( Object m : ss.path_hooks ){
            if( m instanceof ModulePathHook )
                return;
        }

        ss.path_hooks.append( new ModulePathHook( scope ) );
    }

    public void addDependency( String to, String importer ){
        _checkModules();
        ((PythonModuleTracker)pyState.modules).addDependency( to , importer );
    }

    public void addRecursive( String name , AppContext ac ){
        ((PythonModuleTracker)pyState.modules).addRecursive( name , ac );
    }

    public AppContext getContext(){
        return _context;
    }

    /**
     * Set output to an AppRequest.
     *
     * Replace the Python sys.stdout with a file-like object which
     * actually prints to an AppRequest stream.
     */
    public void replaceOutput(){
        PyObject out = pyState.stdout;
        if ( ! ( out instanceof MyStdoutFile ) ){
            pyState.stdout = new MyStdoutFile();
        }
    }

    @ExposedType(name="_10gen_stdout")
    public static class MyStdoutFile extends PyFile {
        static PyType TYPE = Python.exposeClass(MyStdoutFile.class);
        MyStdoutFile(){
            super( TYPE );
        }
        @ExposedMethod
        public void flush(){}

        @ExposedMethod
        public void _10gen_stdout_write( PyObject o ){
            if ( o instanceof PyUnicode ){
                _10gen_stdout_write(o.__str__().toString());
            }
            else if ( o instanceof PyString ){
                _10gen_stdout_write(o.toString());
            }
            else {
                throw Py.TypeError("write requires a string as its argument");
            }
        }

        @ExposedMethod(names={"__str__", "__repr__"})
        public String toString(){
            return "<open file '_10gen.apprequest', mode 'w'>";
        }

        public Object __tojava__( Class cls ){
            return this;
        }

        final public void _10gen_stdout_write( String s ){
            AppRequest request = AppRequest.getThreadLocal();

            if( request == null )
                // Log
                _log.info( s );
            else{
                request.print( s );
            }
        }

        public void write( String s ){
            _10gen_stdout_write( s );
        }
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
            PyObject locals = ( argc > 2 ) ? args[2] : null;
            PyObject fromlist = (argc > 3) ? args[3] : null;

            SiteSystemState sss = Python.getSiteSystemState( null , Scope.getThreadLocal() );
            if( DEBUG ){
                PySystemState current = Py.getSystemState();
                PySystemState sssPy = sss.getPyState();
                System.out.println("Overrode import importing. import " + args[0]);
                System.out.println("globals? " + (globals == null ? "null" : "not null, file " + globals.__finditem__("__file__")));
                System.out.println("Scope : " + Scope.getThreadLocal() + " PyState: " + sssPy + "  id: " + __builtin__.id(sssPy) + " current id: " + __builtin__.id(current) );
                System.out.println("Modules are " + sssPy.modules);
            }

            AppContext ac = sss.getContext();
            PyObject targetP = args[0];
            if( ! ( targetP instanceof PyString ) )
                throw new RuntimeException( "first argument to __import__ must be a string, not a "+ targetP.getClass());
            String target = targetP.toString();

            PyObject siteModule = null;
            PyObject m = null;
            // import <sitename> -- why isn't this a meta_path hook?
            if( target.indexOf('.') != -1 ){
                String firstPart = target.substring( 0 , target.indexOf('.'));
                if( ac != null && firstPart.equals( ac.getName() ) ){
                    siteModule = sss.getPyState().modules.__finditem__( firstPart.intern() );
                    if( siteModule == null ){
                        siteModule = new PyModule( firstPart );
                        sss.getPyState().modules.__setitem__( firstPart.intern() , siteModule );
                    }
                    target = target.substring( target.indexOf('.') + 1 );
                    args[0] = new PyString( target );
                    // Don't recur -- just allow one replacement
                    // This'll still do meta_path stuff, but at least it won't
                    // let you do import sitename.sitename.sitename.foo..
                }
            }

            // If you're in a core-module called foo, in a package
            // called bar.baz, and you do an import for moo, and there is
            // no sensible choice for foo.bar.moo or whatever,
            // we "rewrite" the import to import foo.moo and return that.
            //
            // Sensible in this case means:
            // 1) something on sys.path, excepting the user directory
            // 2) something in the current directory
            //
            // If we're in a core module, we should never import code from
            // the user's directory.
            if( m == null ){
                m = tryModuleRewrite( sss , target , globals , locals , fromlist );
            }

            if( m == null ){
                PySystemState oldPyState = Py.getSystemState();
                try {
                    Py.setSystemState( sss.getPyState() );
                    m = _import.__call__( args, keywords );
                }
                finally {
                    Py.setSystemState( oldPyState );
                }
            }
            return trackDependency( sss , globals , target , siteModule , m , fromlist );
        }

        /**
         * Mark a dependency in a given system state, based on information
         * taken from the import context.
         */
        PyObject trackDependency( SiteSystemState sss , PyObject globals , String target , PyObject siteModule , PyObject m , PyObject fromlist ){
            if( globals == null ){
                // Only happens (AFAICT) from within Java code.
                // For example, Jython's codecs.java calls
                // __builtin__.__import__("encodings");
                // Python calls to __import__ provide an empty Python dict.
                return _finish( target , siteModule , m );
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
                    System.err.println("Can't figure out where the call to import " + target + " came from! Import tracking is going to be screwed up!");
                    return _finish( target, siteModule, m );
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
                    System.err.println("Totally unable to figure out how import to " + target + " came about. Import tracking is going to be screwed up.");
                }
            }

            // We want to find the module that was actually imported so we can
            // get its name for import tracking. If module package.foo does
            // "import baz", it might get package.baz, and we want to record
            // this correctly.
            //
            // We have to return m, but that might not be the module
            // named by the import.  If we got "import foo.bar", m =
            // foo, but we want to get bar.__name__. So we have to
            // search through modules to get to the innermost.  But if
            // we got "from foo import bar", m = bar, and we don't
            // want to do anything. Ahh, crappy __import__ semantics..
            // For more information see
            // http://docs.python.org/lib/built-in-funcs.html
            //
            // Rather than diving through a bunch of modules, we just
            // compute the right name.  This way, recursive imports
            // where some module isn't yet in its package don't screw
            // everything up.
            //
            // We got an import for foo.bar.baz, and we hold now a
            // module that may be package.subpackage.foo. We want to
            // get the name package.subpackage.foo, which should be
            // foo.__name__, and add ".bar.baz" so we get the full
            // module name package.subpackage.foo.bar.baz.

            String imported = null;
            if( fromlist != null && fromlist.__len__() > 0 ){
                // But if we got an import "from foo.bar.baz import thingy",
                // we're holding foo.bar.baz, so we just get the name from
                // m.
                PyObject __name__ = m.__findattr__("__name__");
                if( __name__ == null ){
                    throw new RuntimeException("imported a module without a __name__ : " + m);
                }
                imported = __name__.toString();
            }
            else {
                String startName = m.__findattr__("__name__").toString();
                String [] modNames = target.split("\\.");

                // We got an import for "foo.bar". But Jython might
                // have implemented foo using a Java module FooModule.
                // But of course, sys.modules would only have
                // "foo.bar", not "FooModule.bar", because otherwise
                // it would be ridiculous.  This only seems to happen
                // with time -> Time and _random -> RandomModule, but
                // it could conceivably happen with other modules.
                //
                // If we import foo.bar.baz we should get a "foo"
                // module.  That means its __name__ is "foo" or its
                // __name__ is "something.foo".

                if( ! ( startName.endsWith("."+modNames[0]) || startName.equals(modNames[0]) ) ){
                    if( DEBUG )
                        System.out.println("Did an import for " + target + " but got " + startName );
                    // Hopefully it was just an import for foo, and we
                    // got Foo.  We'll just look up "foo" in
                    // sys.modules and hope for the best.
                    startName = target;
                }
                else {
                    // OK, we got "package.foo", let's add ".bar".
                    for( int i = 1; i < modNames.length; ++i ){
                        startName += "." + modNames[i];
                    }
                }

                imported = startName;
            }

            if( imported == null ) return _finish( target , siteModule , m );

            // Add a plain old JXP dependency on the file that was imported
            // Not sure if this is helpful or not
            // Can't do this right now -- one TrackImport is created for all
            // PythonJxpSources. FIXME.
            //addDependency( to.toString() );

            // Add a module dependency -- module being imported was imported by
            // the importing module.
            // Don't add dependencies to _10gen. FIXME: other "virtual"
            // modules should be OK.
            if( ! ( m instanceof PyModule && ((PyModule)m).__dict__ instanceof PyJSObjectWrapper ) )
                sss.addDependency( imported , importer.toString() );

            return _finish( target , siteModule , m );

            //PythonJxpSource foo = PythonJxpSource.this;
        }

        public PyObject _finish( String target , PyObject siteModule , PyObject result ){
            if( siteModule == null ) return result;
            // We got an import for sitename.foo, and result is <module foo>.
            // siteModule is <module sitename>. target is "foo".
            int dot = target.indexOf( '.' );
            String firstPart = target;
            if( dot != -1 )
                firstPart = target.substring( 0 , dot );
            siteModule.__setattr__( firstPart , result );
            return siteModule;
        }

        public PyObject tryModuleRewrite(SiteSystemState sss , String target , PyObject globals , PyObject locals , PyObject fromlist ){
            // if __name__ indicates we're in core-module named foo.bar.baz,
            // and target is "moo.boo.zoo",
            // check foo.moo.boo.zoo and try that
            if( globals == null ) return null;
            PyObject __name__P = globals.__finditem__("__name__");
            if( ! ( __name__P instanceof PyString ) ) return null;
            String __name__ = __name__P.toString();
            int period = __name__.indexOf('.');
            if( period == -1 ) return null;

            String fooName = __name__.substring( 0 , period );
            for( JSLibrary key : sss._loaded.keySet() ){
                String name = sss._loaded.get( key );
                if( ! name.equals( fooName ) ) continue;

                String lastWord = target;
                File directoryF = key.getRoot();

                if( target.indexOf( '.' ) > -1 ){
                    String directory = target.substring( 0 , target.lastIndexOf('.') );
                    directory = directory.replaceAll("\\.", "/");
                    lastWord = target.substring( target.lastIndexOf( '.' ) + 1 );
                    directoryF = new File( directoryF , directory );
                }
                if( ! directoryF.exists() ) continue;

                // FIXME: other endings for Python files?
                // FIXME: check that the directoryF/lastword is directory, or directoryF/lastword.py isn't?
                if( ! ( new File( directoryF , lastWord ).exists() || new File( directoryF , lastWord + ".py" ).exists() ) ) continue;

                // FIXME: If we get here and we fail, we should really
                // throw an error! Otherwise we end up with misleading
                // error messages like "cannot import webob"
                if( DEBUG ){
                    System.out.println("Got " + directoryF + "/" + lastWord + " from " + sss._loaded);
                    System.out.println("Returning rewrite loader for " + fooName + "." + target);
                }

                PySystemState oldPyState = Py.getSystemState();
                PyObject m = null;
                PyString newTarget = new PyString( fooName + "." + target );
                try {
                    Py.setSystemState( sss.getPyState() );
                    m = _import.__call__( newTarget , globals , locals , fromlist );
                    if( fromlist != null && fromlist.__len__() != 0 ) return m;
                    // we just ran an import for foo.moo.boo.zoo, but the
                    // user just wanted moo.boo.zoo, so let's fetch foo.moo from
                    // sys.modules

                    m = sss.getPyState().modules.__finditem__( newTarget );
                    return m;
                }
                finally {
                    Py.setSystemState( oldPyState );
                }
            }

            return null;
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
         * @param args {PyString} name of the module to find
         * @param keywords {PyList} optional; the __path__ of the module
         */
        @ExposedMethod(names={"find_module"})
        public PyObject find_module( PyObject args[] , String keywords[] ){
            int argc = args.length;
            assert argc >= 1;
            assert args[0] instanceof PyString;
            String modName = args[0].toString();
            PyList __path__ = null;
            if( args.length > 1 && args[1] instanceof PyList ) __path__ = (PyList)args[1];
            if( DEBUG ){
                System.out.println( "meta_path " + __builtin__.id(this) + " looking for " + modName + " " + __path__);
            }

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

            if( modName.startsWith("core.") ){
                int period = modName.indexOf('.');
                String path = modName.substring( period + 1 );
                path = path.replaceAll( "\\." , "/" );
                return new JSLibraryLoader( _core, path );
            }

            if( DEBUG ){
                System.out.println( "meta_path hook didn't match " + modName );
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
         * @param name {PyString} the full name of the module
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

    @ExposedType(name="_10gen_module_js_loader")
    public class JSLibraryLoader extends PyObject {
        JSLibrary _root;
        String _path;
        public JSLibraryLoader( JSLibrary root , String path ){
            _root = root;
            _path = path;
        }

        @ExposedMethod
        public PyModule load_module( String name ){
            PyModule mod = imp.addModule( name );
            PyObject __path__ = mod.__findattr__( "__path__".intern() );
            if( __path__ != null ) return mod;

            PyObject pyName = mod.__findattr__( "__name__" );
            Object o = _root.getFromPath( _path , true );
            PyList pathL = new PyList( );
            if( o instanceof JSFileLibrary ){
                JSFileLibrary lib = (JSFileLibrary)o;
                pathL.append( new PyString( lib.getRoot().toString() ) );
            }

            if( o instanceof JSFunction && ((JSFunction)o).isCallable() ){
                run( mod , (JSFunction)o );
            }

            mod.__setattr__( "__file__".intern() , new PyString( _root + ":" + _path ) );
            mod.__setattr__( "__path__".intern() , pathL );
            mod.__setattr__( "__name__".intern() , pyName );
            return mod;
        }

        public void run( PyModule mod , JSFunction f ){
            Scope pref = _scope.getTLPreferred();
            Scope s = _scope.child();
            s.setGlobal( true );

            try {
                _scope.setTLPreferred( null );
                f.call( s );
                mod.__dict__ = new PyJSObjectWrapper( s );
            }
            finally {
                _scope.setTLPreferred( pref );
            }
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
         * @param name {PyString} the full name of the module
         * @return PyModule
         */
        @ExposedMethod(names={"load_module"})
        public PyObject load_module( String name ){
            PyObject m = __builtin__.__import__(_realName );
            return pyState.modules.__finditem__( _realName );
        }
    }

    public class ModulePathHook extends PyObject {
        Scope _scope;
        ModulePathHook( Scope s ){
            _scope = s;
        }

        @ExposedMethod
        public PyObject __call__(PyObject s){
            if( s.toString().equals(COREMODULES_STRING) )
                return new CoreModuleFinder( _scope );
            throw Py.ImportError("this isn't my magic cookie");
        }
    }

    /**
     * Finder which checks in core.modules to satisfy imports.
     *
     * FIXME: Just get the root file from the module config, don't bother with
     * casting through JSLibrary.
     */
    public class CoreModuleFinder extends PyObject {
        Scope _scope;
        JSLibrary _coreModules;
        CoreModuleFinder( Scope s ){
            _scope = s;
            _coreModules = null;
            Object core = s.get("core");
            if( core instanceof JSObject ){
                Object coremodules = ((JSObject)core).get("modules");
                if( coremodules instanceof JSLibrary ){
                    _coreModules = (JSLibrary)coremodules;
                }
            }
        }

        @ExposedMethod(names={"find_module"})
        public PyObject find_module( PyObject args[] , String keywords[] ){
            int argc = args.length;
            assert argc >= 1;
            assert args[0] instanceof PyString;
            String modName = args[0].toString();

            if( _context == null ) return Py.None;

            Object packages = _context.getConfigObject("packages");
            if( packages == null ){
                if( DEBUG )
                    System.out.println("No packages specified in _config!");
                return Py.None;
            }

            if( ! ( packages instanceof JSObject ) ){
                System.out.println("Warning: Couldn't parse packages specification in _config!");
                return Py.None;
            }

            Object packageSpec = ((JSObject)packages).get( modName );
            if( packageSpec == null ){
                if( DEBUG )
                    System.out.println("Package " + modName + " is not defined in package spec!");
                return Py.None;
            }


            // FIXME: refactor out _loaded.put (somehow)
            if( packageSpec instanceof String || packageSpec instanceof JSString ){
                String packageName = packageSpec.toString();
                String toLoad = null;

                if( ModuleRegistry.getARegistry( _context ).getConfig( "py-"+packageName ) != null ){
                    toLoad = "py-"+ packageName;
                }
                else if( ModuleRegistry.getARegistry( _context ).getConfig( packageName ) != null ){
                    toLoad = packageName;
                }

                if( toLoad != null ){
                    Object foo = _coreModules.getFromPath( toLoad , true );
                    if( foo instanceof JSLibrary ){
                        JSLibrary lib = (JSLibrary)foo;
                        _loaded.put( lib , modName );
                        return new LibraryModuleLoader( lib );
                    }
                }
            }

            else if( packageSpec instanceof JSObject ){
                JSObject packageJ = (JSObject)packageSpec;
                Object moduleObj = packageJ.get( "module" );
                Object pathObj = packageJ.get( "path" );
                if( moduleObj == null ){
                    throw _missingField(modName, "module");
                }

                if( pathObj == null ){
                    throw _missingField(modName, "path");
                }

                if( ! ( moduleObj instanceof String || moduleObj instanceof JSString ) ){
                    throw new RuntimeException("Can't figure out how to handle module of type " + moduleObj.getClass() + " -- expected String (in package " + modName + ", field 'module')");
                }
                if( ! ( pathObj instanceof String || pathObj instanceof JSString ) ){
                    throw new RuntimeException("Can't figure out how to handle path of type " + pathObj.getClass() + " -- expected String (in package " + modName + ", field 'path')");
                }

                String module = moduleObj.toString();
                String path = pathObj.toString();

                Object foo = _coreModules.getFromPath( module , true );
                if( ! ( foo instanceof JSLibrary ) ){
                    throw new RuntimeException("problem loading core-module " + module + " -- got " + foo + " instead of JSLibrary");
                }

                JSLibrary lib = (JSLibrary)foo;

                if( path.equals("") ){
                    _loaded.put( lib , modName );
                    return new LibraryModuleLoader( (JSLibrary) lib );
                }

                Object subdir = lib.getFromPath( path , true );

                if( subdir == null ){
                    throw new RuntimeException( "couldn't find subdirectory " + path + " in module " + module );
                }

                if( ! ( subdir instanceof JSLibrary ) ){
                    throw new RuntimeException( "file was not a subdirectory at module = "+ module + ", path = " + path );
                }

                _loaded.put( (JSLibrary) subdir , modName );
                return new LibraryModuleLoader( (JSLibrary)subdir );
            }

            else {
                throw new RuntimeException( "couldn't figure out how to handle package spec of type " + packageSpec.getClass() );
            }

            if( DEBUG ){
                System.out.println( "core-modules finder didn't match " + modName );
            }
            return Py.None;
        }

        RuntimeException _missingField(String moduleName, String fieldName ){
            return new RuntimeException( "package definition for module " + moduleName + " is missing field " + fieldName );
        }
    }

    public long approxSize( IdentitySet seen ){
        return JSObjectSize.size( _log , seen ) +
            JSObjectSize.size( globals , seen ) +
            JSObjectSize.size( pyState , seen ) +
            JSObjectSize.size( _context , seen ) +
            JSObjectSize.size( _scope , seen );
    }

    final static Logger _log = Logger.getLogger( "python" );
    final public PyObject globals;
    private PySystemState pyState;
    private AppContext _context;
    private Scope _scope;
    Map<JSLibrary, String> _loaded = new HashMap<JSLibrary, String>();

}
