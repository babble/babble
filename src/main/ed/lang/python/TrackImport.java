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

import java.util.*;
import java.io.*;

import org.python.core.*;

import ed.js.*;
import ed.log.*;
import ed.js.engine.*;
import ed.appserver.*;

public class TrackImport extends PyObject {
    static final boolean DEBUG = Boolean.getBoolean( "DEBUG.TRACKIMPORT" );
    public final Logger _log;
    PyObject _import;
    TrackImport( PyObject importF ){
        _import = importF;
        _log = Logger.getRoot().getChild("python_import");
    }

    public PyObject __call__( PyObject args[] , String keywords[] ){
        int argc = args.length;
        // Second argument is the dict of globals. Mostly this is helpful
        // for getting context -- file or module *doing* the import.
        PyObject globals = ( argc > 1 ) ? args[1] : null;
        PyObject locals = ( argc > 2 ) ? args[2] : null;
        PyObject fromlist = (argc > 3) ? args[3] : null;

        SiteSystemState sss = Python.getSiteSystemState( null , Scope.getThreadLocal() );
        /* Call from within Java. We can't be sure that this'll only
         * get builtin (i.e. completely static and unchanging)
         * modules, but we can't really do import tracking since we
         * can't flush the Java code. So just get out fast, without trying
         * anything fancy.
         */
        if( globals == null ){
            return tryFallThrough( sss , args, keywords );
        }

        AppContext ac = sss.getContext();
        PyObject targetP = args[0];
        if( ! ( targetP instanceof PyString ) )
            throw new RuntimeException( "first argument to __import__ must be a string, not a "+ targetP.getClass());
        String target = targetP.toString();

        if( target.equals( "__main__" ) ){
            _log.warn("importing __main__ is almost certainly going to fail in a multithreaded context");
        }

        PyObject siteModule = null;
        PyObject m = null;

        PyObject __name__P = null;
        if( globals != null && globals != Py.None )
            __name__P = globals.__finditem__("__name__");
        String __name__ = null;

        // whether we're excuting an explicit __import__ method
        boolean explicit = false;
        if( __name__P instanceof PyString ){
            __name__ = __name__P.toString();
        }
        else {
            __name__ = getName( globals , target );
            explicit = true;
        }

        if( DEBUG ){
            PySystemState current = Py.getSystemState();
            PySystemState sssPy = sss.getPyState();
            System.out.print("Overrode import importing. import " + target + " from " + __name__);
            if( explicit )
                System.out.print(" (explicit)");
            System.out.println(" on " + sssPy.path + " at " + sssPy.getCurrentWorkingDir());
        }

        if( explicit ){
            // Check site imports first -- perhaps some core-module is trying
            // to load user code
            m = trySiteImport( sss , target , __name__ , explicit , globals , locals , fromlist , ac );
        }

        if( m == null ){
            m = tryImportSitename( sss , target , globals , locals , fromlist , ac );
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
            m = tryModuleRewrite( sss , target , __name__ , explicit , globals , locals , fromlist );
        }

        if( m == null ){
            m = tryFallThrough(sss, args, keywords);
        }

        return trackDependency( sss , globals , target , siteModule , m , fromlist );
    }

    /**
     * Struggle to get a sensible __name__ attribute from the context of an
     * __import__.
     *
     * @param globals the globals dictionary (context)
     * @param target  the module that we wanted to import (for error reporting)
     */
    String getName( PyObject globals , String target ){
        // FIXME: PyUnicode?
        // This can only happen from Java code, as far as I can tell.
        // For example, Jython's codecs.java calls
        // __builtin__.__import__("encodings").  Python calls to
        // __import__ that don't provide an argument get supplied with
        // an empty Python dict. (And there's no way Python can
        // explicitly provide a null.)

        PyObject importer;
        if( globals != null && globals != Py.None ){
            importer = globals.__finditem__( "__name__".intern() );
            if( importer instanceof PyString ){
                return importer.toString();
            }

            if( DEBUG ){
                System.out.println("TrackImport importing " + target + ": Couldn't understand __name__ in globals: " + importer + " -- trying frame");
            }
        }

        // Globals was empty? Maybe we were called "manually" with
        // __import__, or maybe import is happening from an exec()
        // or something.
        // Let's try to get the place that called the import manually
        // and hope for the best.
        PyFrame f = Py.getFrame();
        if( f == null ){
            // No idea what this means
            System.err.println("TrackImport importing " + target + ": Can't figure out where the call to import came from! Import tracking is going to be screwed up!");
            return null;
        }

        globals = f.f_globals;
        importer = globals.__finditem__( "__name__".intern() );
        if( importer instanceof PyString )
            return importer.toString();

        // Probably an import from within an exec("foo", {}).
        // Let's go for broke and try to get the filename from
        // the PyFrame. This won't be tracked any further,
        // but that's fine -- at least we'll know which file
        // needs to be re-exec'ed (e.g. for modjy).
        // FIXME: exec('import foo', {}) ???
        //   -- filename is <string> or what?
        PyTableCode code = f.f_code;
        if( code.co_filename != null )
            return code.co_filename;

        System.err.println("TrackImport importing " + target + ": Totally unable to figure out how import to " + target + " came about. Import tracking is going to be screwed up.");

        return null;
    }

    /**
     * Mark a dependency in a given system state, based on information
     * taken from the import context.
     */
    PyObject trackDependency( SiteSystemState sss , PyObject globals , String target , PyObject siteModule , PyObject m , PyObject fromlist ){
        if( globals == null ){
            // Only happens (AFAICT) from within Java code.
            // Jython won't import user code by accident (hopefully),
            // so we shouldn't need to track this import.
            return _finish( target , siteModule , m );
        }

        if( m == null ){
            // FIXME: Track failed import
            return _finish( target , siteModule , m );
        }

        // gets the module name -- __file__ is the file
        String importer = getName( globals , target );
        if( importer == null )
            // Error was reported by getName()
            return _finish( target , siteModule , m );

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
            PyObject __name__ = m.__findattr__("__name__");
            if( __name__ == null ){
                throw new RuntimeException("imported a module differently without __name__ : " + m );
            }
            String startName = __name__.toString();
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

        PyObject module = sss.getPyState().modules.__finditem__( imported );
        if( module == null ){
            module = m;
            if( ! ( m instanceof PyJavaClass || m instanceof PyJavaPackage ) ){
                System.out.println("Possible error -- couldn't find "+ imported + " after doing import for " + target + " " + m );
            }
        }
        if( module != null ){
            PyObject __file__P = module.__findattr__( "__file__" );
            if( __file__P instanceof PyString ){
                String __file__ = __file__P.toString();
                if( __file__.equals( SiteSystemState.currentlyRunning.get() ) ){
                    _log.warn("Imported " + module + " when running " + __file__ + " -- this could indicate a possible reentrancy issue.");
                }
            }
        }

        // Add a module dependency -- module being imported was imported by
        // the importing module.
        // Don't add dependencies to _10gen. FIXME: other "virtual"
        // modules should be OK.
        if( ! ( m instanceof PyModule && ((PyModule)m).__dict__ instanceof PyJSObjectWrapper ) )
            sss.addDependency( imported , importer );

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

    public PyObject tryModuleRewrite(SiteSystemState sss , String target ,
                                     String __name__, boolean explicit, PyObject globals , PyObject locals , PyObject fromlist ){
        // if __name__ indicates we're in core-module named foo.bar.baz,
        // and target is "moo.boo.zoo",
        // check foo.moo.boo.zoo and try that
        if( globals == null || globals == Py.None ) return null;

        PyObject __path__P = globals.__finditem__("__path__");
        if( __path__P instanceof PyList ){
            String __path__ = ((PyList)__path__P).get(0).toString();
            if( relativeFile( __path__ , target ) )
                return null;
        }

        PyObject __file__P = globals.__finditem__("__file__");
        if( __file__P instanceof PyString ){
            String __file__ = __file__P.toString();
            if( __file__.indexOf( '/' ) != -1 )
                __file__ = __file__.substring( 0 , __file__.lastIndexOf('/') );
            if( relativeFile( __file__ , target ) )
                return null;
        }

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

                int dot = target.indexOf('.');
                String moo = target;
                if( dot > -1 ){
                    moo = target.substring( 0 , dot );
                }
                PyString foomoo = new PyString( fooName + "." + moo );
                m = sss.getPyState().modules.__finditem__( foomoo );
                return m;
            }
            finally {
                Py.setSystemState( oldPyState );
            }
        }

        return null;
    }

    /**
     * Return true if something that might be a Python file called target
     * exists in the directory named by dir.
     */
    public boolean relativeFile( String dir , String target ){
        target = target.replaceAll( "\\." , "/" );
        return new File( dir , target ).exists() || new File( dir , target+".py" ).exists();

    }

    /**
     * Simplest __import__ possibility -- just fall through to the default
     * import implementation.
     */
    PyObject tryFallThrough(SiteSystemState sss, PyObject[] args, String[] keywords){
        PyObject m = null;
        PySystemState oldPyState = Py.getSystemState();
        try {
            Py.setSystemState( sss.getPyState() );
            m = _import.__call__( args, keywords );
        }
        finally {
            Py.setSystemState( oldPyState );
        }
        Python.checkSafeImport( m );
        return m;
    }

    /**
     * We add some magic to make imports for "sitename.foo" and "foo" get
     * the same module.
     */
    public PyObject tryImportSitename( SiteSystemState sss , String target , PyObject globals , PyObject locals , PyObject fromlist , AppContext ac ){
        // We want to import "real files" in the directory, but
        // prohibit other kinds of loading, i.e. from core or a
        // core-module.  This means however we do this, we can't rely
        // on any tricks that recurse.
        String original = target;
        int dot = target.indexOf('.');
        if( dot == -1 ) return null;

        String firstPart = target.substring( 0 , dot );
        if( ac == null || ! firstPart.equals( ac.getName() ) ) return null;

        target = target.substring( target.indexOf('.') + 1 );
        // I only need to take care of the files "one level deep" here --
        // after that, the built-in stuff should 'just work'.
        if( target.indexOf( '.' ) != -1 ) return null;

        PyObject modules = sss.getPyState().modules;

        PyObject siteModule = modules.__finditem__( Py.newString( ac.getName() ) );
        PyString originalP = Py.newString( original );
        if( modules.__finditem__( originalP ) != null ){
            PyObject originalM = modules.__finditem__( originalP );
            if( fromlist != null && fromlist.__len__() != 0 )
                return originalM;
            return siteModule;
        }

        PyString newTarget = new PyString( target );

        // existing points to sitename.foo
        PyObject existing = modules.__finditem__( newTarget );
        if( existing == null ){
            // Have to load this "the hard way", including calling
            // loadFromSource and setting an attribute on the site module
            existing = ImportHelper.loadFromSource( sss.getPyState() , target , target , ac.getRoot() );
            siteModule.__setattr__( target.intern() , existing );
        }

        // alias new to old
        modules.__setitem__( original , existing );
        if( fromlist != null && fromlist.__len__() != 0 ) return existing;
        return siteModule;
    }

    /**
     * If someone does an explicit __import__("foo", {}, {}, []) or whatever,
     * we assume they're looking in the site first.
     */
    public PyObject trySiteImport( SiteSystemState sss , String target ,
                                   String __name__ , boolean explicit , PyObject globals , PyObject locals , PyObject fromlist , AppContext ac ){
        // FIXME: This might only work for one-level-deep modules
        if( ac == null ) return null;
        PyObject modules = sss.getPyState().modules;
        PyObject existing = modules.__finditem__( Py.newString( target ) );
        if( existing != null ) return existing;

        if( relativeFile( ac.getRoot() , target ) ){
            existing = ImportHelper.loadFromSource( sss.getPyState() , target , target , ac.getRoot() );
            sss.getPyState().modules.__setitem__( Py.newString( target ) , existing );
            return existing;
        }

        return null;
    }

}
