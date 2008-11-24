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
import org.python.core.*;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Set;

public class PythonCGIAdapter extends CGIAdapter {

    final File _file;
    final JSFileLibrary _lib;
    final static Logger _log = Logger.getLogger("pythonCGI");

    private PyCode _code;
    private long _lastCompile;

    static {
        System.setProperty("python.cachedir", ed.io.WorkingFiles.getTypeDir( "jython-cache" ) + Version.PY_VERSION);
    }

    public PythonCGIAdapter(File f, JSFileLibrary lib) {
        _file = f;
        _lib = lib;
    }

    public void handleCGI(EnvMap env, InputStream stdin, OutputStream stdout, AppRequest ar) {

        if (env == null) {
            throw new RuntimeException("Error : PythonCGIAdapater.handleCGI() invoked w/ null EnvMap");
        }

        final AppContext ac = getAppContext();

        SiteSystemState ss = Python.getSiteSystemState(ac, null);

        /*
         * "un-welded" scopes
         */
        PyObject globals = new PyDictionary();

        /*
         * create a standard Py dictionary to hold the env data.  Give it to the
         * CGITLSData
         */
        PyDictionary pd = new PyDictionary();

        for(String s : env.keySet()) {
            pd.__setitem__(s, Py.newString((String) env.get(s)));
        }

        PyObject os = PySystemState.builtins.__finditem__("__import__").__call__( new PyObject[] { Py.newString("os"), null, null, null});
        os.__setattr__("environ", new PyTLSProxyDict());

        /*
         *  create a threadlocal writer for the output stream.  Then hand it a PyFile
         *  wrapped around the input stream, as that's sufficient (so far) for dealing
         *  with CGI reading on the stream
         */
        CGITLSData cgiosw = new CGITLSData(new PyFile(stdin), stdout, pd);

        // TODO - these don't change ever for a site..

        ss.getPyState().stdout = new PythonCGIOutFile();
        ss.getPyState().stdin = new PyTLSProxyFile();

        try {
            PythonJxpSource.runPythonCode(_getCode(), ac, ss, globals, _lib, _file, true);
        }
        catch (IOException e) {
            // TODO - fix
            e.printStackTrace();
        }
        finally {
            cgiosw.unset();
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
    public static class CGITLSData {

        protected final PyFile _inFile;

        protected final OutputStream _out;
        protected final PyDictionary _pyDict;

        static ThreadLocal<CGITLSData> _tl = new ThreadLocal<CGITLSData>();

        public CGITLSData(PyFile infile, OutputStream o, PyDictionary dict) {
            _inFile = infile;
            _out = o;
            _pyDict = dict;
            _tl.set(this);
        }

        public OutputStream getOutputStream() {
            return _out;
        }

        public PyFile getInputFile() {
            return _inFile;
        }

        public PyDictionary getPyDict() {
            return _pyDict;
        }

        public static CGITLSData getThreadLocal() {
            return _tl.get();
        }

        public void unset() {
            _tl.remove();   // note added in Java 5
        }
    }

    /**
     *  Mimimalist implementation of a TLS PyDict - we are only implementing
     *  specific methods to be sure we have everything covered.  So expect  to
     *   revisit this as we figure out what real apps need
     */
    public static class PyTLSProxyDict extends PyObject {

        public PyObject __findattr_ex__(String key) {
            return CGITLSData.getThreadLocal().getPyDict().__findattr__(key);
        }

        public PyObject __finditem__(PyObject key) {
            return CGITLSData.getThreadLocal().getPyDict().__finditem__(key);
        }

        public void __setitem__(PyObject key, PyObject value) {
            CGITLSData.getThreadLocal().getPyDict().__setitem__(key, value);
        }

        public boolean __contains__(PyObject key) {
            return CGITLSData.getThreadLocal().getPyDict().has_key(key);
        }
    }

    /**
     *  Proxy file to use for
     */
    public static class PyTLSProxyFile extends PyObject {

        public PyObject __findattr_ex__(String key) {

            return CGITLSData.getThreadLocal().getInputFile().__findattr__(key);
        }
    }
}
