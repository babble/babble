// PythonJxpSource.java

package ed.lang.python;

import java.io.*;
import java.util.*;

import org.python.core.*;

import ed.js.*;
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
    
    protected String getName(){
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
                
                PySystemState ss = Py.getSystemState();
                ss.stdout = _stdout;

                return code.call( args , new String[0] , new PyJSObjectWrapper( s , false ) , new PyObject[0] , null );
            }
        };
    }


    final File _file;
    final JSFileLibrary _lib;

    // static b/c it has to use ThreadLocal anyway

    static class MyStdoutFile extends PyFile {
        MyStdoutFile(){
        }
        
        public void flush(){}

        public void write( String s ){
            AppRequest req = AppRequest.getThreadLocal();
            
            if ( req == null )
                System.out.print( s );
            else 
                req.print( s );
        }
    }
    
    static final MyStdoutFile _stdout = new MyStdoutFile();
}

