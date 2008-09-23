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

import org.python.core.*;

import ed.appserver.*;
import ed.log.*;

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
 * SiteSystemState.state to Py.setSystemState as needed.
 */
public class SiteSystemState {
    SiteSystemState( AppContext ac , PyObject newGlobals ){
        state = new PySystemState();
        globals = newGlobals;
        setupModules();
    }

    /**
     * Set up module interception code.
     *
     * We replace sys.modules with a subclass of PyDictionary so we
     * can intercept calls to import and flush old versions of modules
     * when needed.
     */
    public void setupModules(){
        if( ! ( state.modules instanceof PythonModuleTracker ) ){
            if( state.modules instanceof PyStringMap)
                state.modules = new PythonModuleTracker( (PyStringMap)state.modules );
            else {
                // You can comment out this exception, it shouldn't
                // break anything beyond reloading python modules
                throw new RuntimeException("couldn't intercept modules " + state.modules.getClass());
            }
        }
    }

    private void _checkModules(){
        if( ! ( state.modules instanceof PythonModuleTracker ) ){
            throw new RuntimeException( "i'm not sufficiently set up yet" );
        }
    }

    /**
     * Flush old modules that have been imported by Python code but
     * whose source is now newer.
     */
    public void flushOld(){
        System.out.println("Flushing " + __builtin__.id(state.modules));
        ((PythonModuleTracker)state.modules).flushOld();
    }

    public void addDependency( PyObject to, PyObject importer ){
        _checkModules();
        System.out.println("Adding dependency to " + __builtin__.id(state.modules));
        ((PythonModuleTracker)state.modules).addDependency( to , importer );
    }

    /**
     * Set output to an AppRequest.
     *
     * Replace the Python sys.stdout with a file-like object which
     * actually prints to an AppRequest stream.
     */
    public void setOutput( AppRequest ar ){
        PyObject out = state.stdout;
        if ( ! ( out instanceof MyStdoutFile ) || ((MyStdoutFile)out)._request != ar ){
            state.stdout = new MyStdoutFile( ar );
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

    final static Logger _log = Logger.getLogger( "python" );
    final public PyObject globals;
    final public PySystemState state;
}
