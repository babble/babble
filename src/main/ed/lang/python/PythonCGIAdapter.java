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
import org.python.core.PySystemState;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyFile;
import org.python.core.PyModule;
import org.python.core.PyCode;
import org.python.core.PyDictionary;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Set;

public class PythonCGIAdapter extends CGIAdapter {

    final File _file;
    final JSFileLibrary _lib;
    final static Logger _log = Logger.getLogger("pythonCGI");

    private PyCode _code;
    private long _lastCompile;

    static {
        System.setProperty("python.cachedir", ed.io.WorkingFiles.TMP_DIR + "/jython-cache/" + Version.PY_VERSION);
    }

    public PythonCGIAdapter(File f, JSFileLibrary lib) {
        _file = f;
        _lib = lib;
    }

    public void handleCGI(EnvMap env, InputStream stdin, OutputStream stdout, AppRequest ar) {

        if (env == null) {
            throw new RuntimeException("Error : PythonCGIAdapater.handleCGI() invoked w/ null EnvPam");
        }

        final AppContext ac = getAppContext();

        SiteSystemState ss = Python.getSiteSystemState(ac, null);
        PySystemState pyOld = Py.getSystemState();

        ss.flushOld();

        ss.ensurePath(_lib.getRoot().toString());
        ss.ensurePath(_lib.getTopParent().getRoot().toString());

        /*
        * "un-welded" scopes - if you need to weld, restore the commented out line (gmj)
        */
        PyObject globals = new PyDictionary();
        // PyObject globals = ss.globals;

        PyObject oldFile = globals.__finditem__("__file__");

        PyObject environ = ss.getPyState().getEnviron();

        for (String key : env.keySet()) {
            environ.__setitem__(key.intern(), Py.newString(env.get(key)));
        }

        /*
         *  create a threadlocal writer for the output stream
         *  TODO - need to do for input stream as it will suffer the same problem
         */
        CGIOutputStreamWriter cgiosw = new CGIOutputStreamWriter(stdout);

        ss.getPyState().stdout = new PythonCGIOutFile();
        ss.getPyState().stdin = new PyFile(stdin);

        try {
            Py.setSystemState(ss.getPyState());

            globals.__setitem__("__file__", Py.newString(_file.toString()));
            PyModule module = new PyModule("__main__", globals);

            PyObject locals = module.__dict__;
            Py.runCode(_getCode(), locals, globals);
        }
        catch (IOException e) {
            // TODO - fix
            e.printStackTrace();
        }
        finally {

            cgiosw.unset();

            if (oldFile != null) {
                globals.__setitem__("__file__", oldFile);
            } else {
                // FIXME -- delitem should really be deleting from siteScope
                globals.__delitem__("__file__");
                //siteScope.set( "__file__", null );
            }
            Py.setSystemState(pyOld);
        }
    }

    private PyCode _getCode() throws IOException {

        PyCode c = _code;
        final long lastModified = _file.lastModified();
        
        if (c == null || _lastCompile < lastModified) {
            c = Python.compile(_file);
            _code = c;
            _lastCompile = lastModified;
        }
        return c;
    }

    protected String getContent() {
        throw new RuntimeException("you can't do this");
    }

    protected InputStream getInputStream() {
        throw new RuntimeException("you can't do this");
    }

    public long lastUpdated(Set<Dependency> visitedDeps) {
        return _file.lastModified();
    }

    public String getName() {
        return _file.toString();
    }

    public File getFile() {
        return _file;
    }
    
    void addDependency(String to) {
        super.addDependency(new FileDependency(new File(to)));
    }

    /**
     * Threadlocal container to carry the input and output streams
     * because concurrent usage of the jython runtime apparently
     * never occurred to the jython designers
     */
    public static class CGIOutputStreamWriter extends OutputStreamWriter {

        protected OutputStream _out;

        static ThreadLocal<CGIOutputStreamWriter> _tl = new ThreadLocal<CGIOutputStreamWriter>();

        public CGIOutputStreamWriter(OutputStream o) {
            super(o);
            _out = o;
            _tl.set(this);
        }

        public static CGIOutputStreamWriter getThreadLocal() {
            return _tl.get();
        }

        public void unset() {
            _tl.remove();   // note added in Java 5
        }
    }
}
