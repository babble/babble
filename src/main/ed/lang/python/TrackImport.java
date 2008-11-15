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
import ed.js.engine.*;
import ed.appserver.*;

public class TrackImport extends PyObject {
    static final boolean DEBUG = Boolean.getBoolean( "DEBUG.TRACKIMPORT" );
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
        // explicitly provide a null.
        if( globals == null ){
            return null;
        }

        PyObject importer = globals.__finditem__( "__name__".intern() );
        if( importer instanceof PyString ){
            return importer.toString();
        }

        if( DEBUG ){
            System.out.println("TrackImport importing " + target + ": Couldn't understand __name__ in globals: " + importer + " -- trying frame");
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

    public PyObject tryModuleRewrite(SiteSystemState sss , String target , PyObject globals , PyObject locals , PyObject fromlist ){
        // if __name__ indicates we're in core-module named foo.bar.baz,
        // and target is "moo.boo.zoo",
        // check foo.moo.boo.zoo and try that
        if( globals == null ) return null;
        PyObject __name__P = globals.__finditem__("__name__");
        if( __name__P == null ){
            // core-module did an __import__ ?
        }

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