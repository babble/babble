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
import org.python.expose.*;
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
        System.setProperty( "python.cachedir", "/tmp/jxp/jython-cache/" + Version.PY_VERSION );
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

    public synchronized JSFunction getFunction()
        throws IOException {
        
        final PyCode code = _getCode();
        
        return new ed.js.func.JSFunctionCalls0(){
            public Object call( Scope s , Object extra[] ){
                
                PyObject args[] = new PyObject[ extra == null ? 0 : extra.length ];
                for ( int i=0; i<args.length; i++ )
                    args[i] = Python.toPython( extra[i] );
                
                final AppRequest ar = AppRequest.getThreadLocal();
                
                PySystemState ss = Py.getSystemState();
                PyObject out = ss.stdout;
                if ( ! ( out instanceof MyStdoutFile ) || ((MyStdoutFile)out)._request != ar ){
                    ss.stdout = new MyStdoutFile( ar );
                }
                
                String myPath = _lib.getRoot().toString();
                boolean found = false;
                for ( Object o : ss.path ){
                    if ( o.toString().equals( myPath ) ){
                        found = true;
                        break;
                    }
                }
                if ( ! found )
                    ss.path.append( Py.newString( myPath ) );

                Scope pyglobals = s.child( "scope to hold python builtins" );

                PyObject globals = new PyJSScopeWrapper( pyglobals , false );
                Scope tl = pyglobals.getTLPreferred();

                pyglobals.setGlobal( true );
                __builtin__.fillWithBuiltins( globals );
                globals.invoke( "update", PySystemState.builtins );
                pyglobals.setGlobal( false );

                PyModule xgenMod = imp.addModule("_10gen");
                // I know this is appalling but they don't expose this any other
                // way
                xgenMod.__dict__ = globals;

                //Py.initClassExceptions( globals );
                globals.__setitem__( "__file__", Py.newString( _file.toString() ) );
                PyModule module = new PyModule( "main" , globals );

                PyObject locals = module.__dict__;

                return Py.runCode( code, locals, globals );
            }
        };
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
    
    // static b/c it has to use ThreadLocal anyway
    
    final static Logger _log = Logger.getLogger( "python" );
        }
