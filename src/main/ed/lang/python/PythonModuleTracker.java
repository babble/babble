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

public class PythonModuleTracker extends PyStringMap {
    static final boolean DEBUG = Boolean.getBoolean( "DEBUG.PYTHONMODULETRACKER" );

    boolean _flushing = false;

    PythonModuleTracker( PyStringMap old ){
        super();
        update( new PyObject[]{ old }, new String[0] );
    }

    public PyObject __finditem__( String key ){
        return handleReturn( super.__finditem__( key ) );
    }

    public PyObject __finditem__( PyObject key ){
        return handleReturn( super.__finditem__( key ) );
    }

    PyObject handleReturn( PyObject obj ){
        if( ! ( obj instanceof PyModule ) ) return obj;
        // if module is outdated
        PyModule module = (PyModule)obj;
        return module;
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
        String clsPath = filename.replace( ".py" , "$py.class" );
        // Compiled class file -- might not exist
        File clsFile = new File( clsPath );
        
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

        boolean shouldFlush = false;

        Set<String> newer = new HashSet<String>();
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

            if( needsRefresh( __file__.toString() ) ){
                newer.add( s );
            }
        }

        Set<String> flushed = new HashSet<String>();
        Set<String> toAdd = new HashSet<String>();

        toAdd.addAll( newer );
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

    public void addDependency( PyObject module , PyObject importer ){
        String moduleS = module.toString();
        String importerS = importer.toString();
        Set<String> rdeps = _reverseDeps.get( moduleS );
        if( rdeps == null ){
            rdeps = new HashSet<String>();
            _reverseDeps.put( moduleS , rdeps );
        }
        if( DEBUG )
            System.out.println( "Module "+ module + " was imported by module " + importer );
        rdeps.add( importerS );
    }

}
