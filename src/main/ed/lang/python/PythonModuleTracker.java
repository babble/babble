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

    public void flushOld( String rootpath ){

        boolean shouldFlush = false;

        for( Object o : keys() ){
            PyObject obj = super.__finditem__( o.toString() );
            if( ! ( obj instanceof PyModule ) ) continue;
            PyModule mod = (PyModule)obj;
            PyObject __file__ = mod.__dict__.__finditem__( "__file__" );
            if( __file__ == null ){
                continue;
            }

            File pyFile = new File( __file__.toString() );
            String clsPath = __file__.toString().replace( ".py" , "$py.class" );
            File clsFile = new File( clsPath );
            if( clsFile.exists() && 
                pyFile.lastModified() > clsFile.lastModified() ){
                System.out.println("Jerks " + pyFile + " " + clsFile);
                shouldFlush = true;
            }
        }

        if( ! shouldFlush ) return;

        for( Object o : keys() ){

            PyObject obj = super.__finditem__( o.toString() );
            if( ! ( obj instanceof PyModule ) ) continue;
            PyModule mod = (PyModule)obj;
            PyObject __file__ = mod.__dict__.__finditem__( "__file__" );
            if( __file__ == null ){
                continue;
            }

            if( __file__.toString().startsWith( rootpath ) ){
                __delitem__( o.toString() );
            }

        }

    }

    public PyObject importFunction(PyObject origF){
        return new TrackImport( origF );
    }

    class TrackImport extends PyObject {
        PyObject _import;
        TrackImport( PyObject importF ){
            _import = importF;
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


}
