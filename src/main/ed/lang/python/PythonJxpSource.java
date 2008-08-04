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

import ed.js.*;
import ed.log.*;
import ed.js.engine.*;
import ed.lang.*;
import ed.util.*;
import ed.appserver.*;
import ed.appserver.jxp.*;

public class PythonJxpSource extends JxpSource {

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
        
        final PyCode code = Python.compile( _file );
        
        return new ed.js.func.JSFunctionCalls0(){
            public Object call( Scope s , Object extra[] ){
                
                PyObject args[] = new PyObject[ extra == null ? 0 : extra.length ];
                for ( int i=0; i<args.length; i++ )
                    args[i] = Python.toPython( extra[i] );
                
                final AppRequest ar = AppRequest.getThreadLocal();
                
                PySystemState ss = Py.getSystemState();
                if ( ! ( ss instanceof MySystemState ) || ((MySystemState)ss)._request != ar ){
                    ss = new MySystemState( ar );
                    Py.setSystemState( ss );
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

                PyObject globals = new PyJSObjectWrapper( s , false );
                __builtin__.fillWithBuiltins( globals );
                globals.__setitem__( "__file__", Py.newString( _file.toString() ) );
                PyModule module = new PyModule( "main" , globals );

                PyObject locals = module.__dict__;

                return Py.runCode( code, locals, globals );
            }
        };
    }


    final File _file;
    final JSFileLibrary _lib;
    
    // static b/c it has to use ThreadLocal anyway
    
    static class MySystemState extends PySystemState {

        MySystemState( AppRequest request ){
            _request = request;
            stdout = new MyStdoutFile();
        }
        
        class MyStdoutFile extends PyFile {
            public void flush(){}
            
            public void write( String s ){
                if( _request == null )
                    // Log
                    _log.info( s );
                else
                    _request.print( s );
            }
        }
        
        final AppRequest _request;
    }

    final static Logger _log = Logger.getLogger( "python" );


}
