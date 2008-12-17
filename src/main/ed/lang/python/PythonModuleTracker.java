// PythonModuleTracker.java

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

import org.python.core.*;
import java.util.*;
import java.io.*;

import ed.appserver.*;
import ed.util.*;
import ed.security.Security;

/**
 * Replacement class for sys.modules. Can flush outdated modules, and
 * prevents untrusted code from getting access to Java
 * packages/classes.
 */
public class PythonModuleTracker extends PyStringMap {
    static final boolean DEBUG = Boolean.getBoolean( "DEBUG.PYTHONMODULETRACKER" );

    boolean _flushing = false;
    PythonModuleTracker( PyStringMap old ){
        super();
        update( new PyObject[]{ old }, new String[0] );
    }

    public void dropUserModules(){
        // FIXME: PyStringMap, right? Aren't I casting to/from PyString here?
        PyList keys = keys();
        int length = keys.__len__();
        for( int i = 0; i < length; ++i ){
            PyObject key = keys.pyget(i);
            if( key.equals("_10gen") )
                continue; // yeah, whatever
            PyObject val = __finditem__( key );
            PyObject __file__ = val.__findattr__( "__file__" );
            if( __file__ == null || __file__ == Py.None )
                continue;  // builtin; can't be flushed
            if( ! ( __file__ instanceof PyString ) )
                continue; // ??

            String __file__S = __file__.toString();
            if( Security.isInEd( __file__S ) ){
                continue;
            }

            __delitem__( key );
        }
    }

    public PyObject __finditem__( String key ){
        return handleReturn( super.__finditem__( key ) );
    }

    public PyObject __finditem__( PyObject key ){
        return handleReturn( super.__finditem__( key ) );
    }

    PyObject handleReturn( PyObject obj ){
        Python.checkSafeImport( obj );
        if( ! ( obj instanceof PyModule ) ) return obj;
        // if module is outdated
        PyModule module = (PyModule)obj;
        return module;
    }

    public PyObject get( PyObject key ){
        return get( key , Py.None );
    }

    public PyObject get( PyObject key , PyObject missing ){
        return handleReturn( super.get( key , missing ) );
    }

    public PyStringMap copy(){
        return new PythonModuleTracker( this );
    }

    public PyList keys(){
        PyList k = new PyList();
        PyList keys = super.keys();
        int len = keys.__len__();
        for( int i = 0; i < len; i++ ){
            PyObject key = keys.pyget( i );
            PyObject val = null;
            if( key instanceof PyString )
                val = super.__finditem__( key.toString() );
            else
                val = super.__finditem__( key );
            if( Python.isSafeImport( val ) )
                k.append(key);
        }
        return k;
    }

    public PyList items(){
        PyList l = new PyList();
        PyList keys = keys();
        int len = keys.__len__();
        for( int i = 0; i < len ; i++){
            PyObject key = keys.pyget(i);
            PyObject val = null;
            if( key instanceof PyString )
                val = super.__finditem__( key.toString() );
            else
                val = super.__finditem__( key );
            PyTuple t = new PyTuple(key, val);
            l.append(t);
        }
        return l;
    }

    public PyList values(){
        PyList v = new PyList();
        PyList values = super.values();
        int len = values.__len__();
        for( int i = 0; i < len; ++i){
            PyObject val = values.pyget(i);
            if( Python.isSafeImport( val ) )
                v.append(val);
        }
        return v;
    }

    // FIXME: clumsy -- actually implement?
    public PyObject iterkeys(){
        return keys().__iter__();
    }

    public PyObject itervalues(){
        return values().__iter__();
    }

    public PyObject iteritems(){
        return items().__iter__();
    }

    public PyObject pop( PyObject key ){
        return pop( key , Py.None );
    }

    public PyObject pop( PyObject key , PyObject value ){
        PyObject m = super.__finditem__( key );
        // Don't pop unless safe
        if( Python.isSafeImport( m ) )
            return handleReturn( super.pop( key , value ) );
        Python.checkSafeImport( m ); // raises exception
        return null;
    }

    public void __setitem__( String key , PyObject value ){
        super.__setitem__( key , value );
        handleSet( value );
    }

    public void __setitem__( PyObject key , PyObject value ){
        super.__setitem__( key , value );
        handleSet( value );
    }

    void handleSet( PyObject obj ){
        // Thought I could get the __file__ and check timestamp, but at this
        // point in time, the module doesn't have a __file__
    }

    public static boolean needsRefresh( String filename ){
        // Src for file
        File pyFile = new File( filename );
        if( ! pyFile.isAbsolute() ){
            AppContext ac = AppContext.findThreadLocal();
            String root;
            if( ac != null )
                root = ac.getRootFile().toString();
            else
                root = Py.getSystemState().getCurrentWorkingDir();
            pyFile = new File( root , pyFile.toString() );
        }
        String clsPath = pyFile.getPath().replace( ".py" , "$py.class" );
        // Compiled class file -- might not exist
        File clsFile = new File( clsPath );

        if( ! pyFile.exists() ){
            if( DEBUG )
                System.out.println("File was deleted: " + pyFile);
            return true;
        }

        if( clsFile.exists() &&
            pyFile.lastModified() > clsFile.lastModified() ){
            if( DEBUG )
                System.out.println("Newer " + pyFile + " " + clsFile);
            return true;
        }
        return false;
    }

    /**
     * @return the flushed files
     */
    public Set<File> flushOld(){
        return flushOld( null );
    }

    public Set<File> flushOld(Collection<String> modules){

        boolean shouldFlush = false;

        Set<String> newer = new HashSet<String>();
        if( modules != null )
            newer.addAll( modules );
        Set<File> files = new HashSet<File>();

        // Go through each module in sys.modules and see if the file is newer
        for( Object o : keys() ){
            if( ! (o instanceof String) ){
                throw new RuntimeException( "not a string in the keys " + o.getClass() );
            }
            String s = (String)o;
            PyObject obj = super.__finditem__( s );
            if( ! ( obj instanceof PyModule ) )
                continue; // User madness?
            PyModule mod = (PyModule)obj;

            // File the module was loaded from.
            PyObject __file__ = mod.__dict__.__finditem__( "__file__" );
            if( __file__ == null || !( __file__ instanceof PyString ) ){
                // User could have overridden it, in which case, can't do much
                continue;
            }

            if( __file__.toString().equals( SiteSystemState.VIRTUAL_MODULE ) ){
                // FIXME: might have to somehow refresh these someday
                continue;
            }

            if( needsRefresh( __file__.toString() ) ){
                newer.add( s );
                for( Object o2 : keys() ){
                    if( ! (o2 instanceof String) ){
                        throw new RuntimeException( "got a non-string key " + o.getClass() ); // can't happen
                    }
                    String s2 = (String)o2;
                    if( s2.startsWith( s + "." ) ) // "child" of flushed module
                        newer.add( s2 );
                }

            }
        }

        Set<String> flushed = new HashSet<String>();
        Set<String> toAdd = new HashSet<String>();

        toAdd.addAll( newer );
        /*
         * The following if statement is what turns on the aggressive flushing
         * strategy. If you take it out, we'll revert back to our incomplete
         * "try to be smart" code.
         *
         * The problem is that some things need to not be flushed. os.environ
         * gets customized, so flushing it is bad. logging modules
         * need to not get flushed because AE might add a custom logger.
         * So we try to be smart and only flush based on dependencies.
         */
        // if( ! newer.isEmpty() ){
        //     if( DEBUG )
        //         System.out.println("Flushing *everything*");
        //     dropUserModules();
        //     return null;
        // }

        while( ! toAdd.isEmpty() ){
            // FIXME -- guess I should use a queue or something
            String o = toAdd.iterator().next();
            flushed.add( o );
            toAdd.remove( o );
            PyObject obj = super.__finditem__( o );

            if( obj == null ){
                // Not really a module -- someone probably stuck a filename in
                // our rdeps.
                files.add( new File( o ) );
                continue;
            }
            if( ! ( obj instanceof PyModule ) ){
                // user madness?
                // FIXME: go in files?
                continue;
            }
            PyModule mod = (PyModule)obj;

            PyObject __file__ = obj.__findattr__( "__file__" );
            if( __file__ != null )
                files.add( new File( __file__.toString() ) );

            if( DEBUG )
                System.out.println("Flushing " + o);
            if( ! o.equals( "_10gen" ) ) // FIXME: re-add _10gen somehow
                __delitem__( o );

            // Get the set of modules that imported module o
            Set<String> rdeps = _reverseDeps.get( o );
            _reverseDeps.remove( o );
            if( rdeps == null ){
                if( DEBUG )
                    System.out.println("Nothing imported " + o );
                continue;
            }
            for( String s : rdeps ){
                if( DEBUG )
                    System.out.println("module "+ s + " imported " + o );
                toAdd.add( s );
            }

        }

        return files;
    }

    // Stores relationships of "module Y was imported by modules X1, X2, X3.."
    Map<String, Set<String> > _reverseDeps = new HashMap<String, Set<String> >();
    Map<String, Set<String> > _forwardDeps = new HashMap<String, Set<String> >();

    public void addDependency( String moduleS , String importerS ){
        Set<String> rdeps = _reverseDeps.get( moduleS );
        if( rdeps == null ){
            rdeps = new HashSet<String>();
            _reverseDeps.put( moduleS , rdeps );
        }
        if( DEBUG )
            System.out.println( "Module "+ moduleS + " was imported by module " + importerS );
        rdeps.add( importerS );

        Set<String> fdeps = _forwardDeps.get( importerS );
        if( fdeps == null ){
            fdeps = new HashSet<String>();
            _forwardDeps.put( importerS , fdeps );
        }
        fdeps.add( moduleS );
    }

    public void addRecursive( String name , AppContext ac ){
        addRecursive( name , ac , new IdentitySet());
    }

    public void addRecursive( String name , AppContext ac , IdentitySet seen ){
        if( seen.contains( name ) ) return;

        seen.add( name );

        PyObject mod = __finditem__( name );
        if( DEBUG )
            System.out.println("Adding init dependency for " + name + " " + mod);
        if( mod != null ){
            PyObject __file__ = mod.__findattr__( "__file__" );
            if( __file__ != null ){
                File file = new File( __file__.toString() );
                ac.addInitDependency( file );
            }
            else {
                // builtin module; for dependency purposes, it doesn't matter,
                // because how could a builtin module import user code?
            }
        }
        else {
            // This is OK because a JXP doesn't have a module, but imported
            // other modules.
            if( DEBUG )
                System.out.println("Skipping -- no module by that name.");
        }

        Set<String> fdeps = _forwardDeps.get( name );
        if( DEBUG ){
            System.out.println( name + " imported " + fdeps );
        }

        if( fdeps == null ) return; // didn't import any modules
        for( String s : fdeps ){
            addRecursive( s, ac , seen );
        }
    }

}
