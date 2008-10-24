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

import ed.appserver.JSFileLibrary;
import ed.appserver.AppRequest;
import ed.appserver.AppContext;
import ed.appserver.adapter.cgi.EnvMap;
import ed.appserver.adapter.cgi.CGIAdapter;
import ed.util.Dependency;
import ed.log.Logger;
import org.python.Version;
import org.python.expose.ExposedType;
import org.python.expose.ExposedMethod;
import org.python.core.PySystemState;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyFile;
import org.python.core.PyModule;
import org.python.core.PyCode;
import org.python.core.PyType;
import org.python.core.PyUnicode;
import org.python.core.PyString;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Set;

public class PythonCGIAdapter extends CGIAdapter {

    static {
        System.setProperty( "python.cachedir", ed.io.WorkingFiles.TMP_DIR + "/jython-cache/" + Version.PY_VERSION );
    }

    public PythonCGIAdapter( File f , JSFileLibrary lib ){
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

   public void handleCGI(EnvMap env, InputStream stdin, OutputStream stdout, AppRequest ar) {

        final AppContext ac = getAppContext();

        SiteSystemState ss = Python.getSiteSystemState(ac, null);
        PySystemState pyOld = Py.getSystemState();

        ss.flushOld();

        ss.ensurePath( _lib.getRoot().toString() );
        ss.ensurePath( _lib.getTopParent().getRoot().toString() );

        PyObject globals = ss.globals;
        PyObject oldFile = globals.__finditem__( "__file__" );

        if (env != null) {
            PyObject environ = ss.getPyState().getEnviron();

            for (String key : env.keySet()) {
                environ.__setitem__( key.intern() , Py.newString(env.get(key)));
            }

            ss.getPyState().stdout = new CGIOutFile(stdout);
            ss.getPyState().stdin = new PyFile(stdin);
        }

        try {
            Py.setSystemState( ss.getPyState() );

            globals.__setitem__( "__file__", Py.newString( _file.toString() ) );
            PyModule module = new PyModule( "__main__" , globals );

            PyObject locals = module.__dict__;
            Py.runCode(_getCode(), locals, globals );
        }
        catch (IOException e) {
            // TODO - fix
            e.printStackTrace();
        }
        finally {
            if( oldFile != null ){
                globals.__setitem__( "__file__" , oldFile );
            }
            else{
                // FIXME -- delitem should really be deleting from siteScope
                globals.__delitem__( "__file__" );
                //siteScope.set( "__file__", null );
            }
            Py.setSystemState( pyOld );
        }
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

    void addDependency( String to ){
        super.addDependency( new FileDependency( new File( to ) ) );
    }

    // static b/c it has to use ThreadLocal anyway
    final static Logger _log = Logger.getLogger( "pythonCGI" );


    @ExposedType(name="_10gen_cgiout")
    public static class CGIOutFile extends PyFile {

        static PyType TYPE = Python.exposeClass(CGIOutFile.class);

        OutputStreamWriter _osWriter;

        OutputStream _os;

        CGIOutFile(OutputStream outStream){
            super( TYPE );
            _osWriter = new OutputStreamWriter(outStream);
        }
        @ExposedMethod
        public void flush(){
            try {
                _osWriter.flush();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }

        @ExposedMethod
        public void _10gen_cgiout_write( PyObject o ){
            
            if ( o instanceof PyUnicode){
                _10gen_cgiout_write(o.__str__().toString());
            }
            else if ( o instanceof PyString){
                _10gen_cgiout_write(o.toString());
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

        final public void _10gen_cgiout_write( String s ){
            try {
                _osWriter.write(s);
                _osWriter.flush();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }

        public void write( String s ){
            _10gen_cgiout_write( s );
        }
    }
}
