package ed.lang.python;

import ed.appserver.adapter.wsgi.WSGIAdapter;
import ed.appserver.adapter.cgi.EnvMap;
import ed.appserver.AppRequest;
import ed.appserver.JSFileLibrary;
import ed.appserver.AppContext;
import ed.util.Dependency;
import ed.io.StreamUtil;
import ed.js.engine.Scope;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.Properties;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.core.PyFile;
import org.python.util.PythonInterpreter;


/**
 * First cut at a WSGI adapter. Designed to use any simple WSGI framework -
 * currently depends on the simple WSGI framework that's offered in the spec.
 * <p/>
 * Right now, this only handles apps that have a clear entry function.  WSGI can also
 * do Class-based apps (I grok) and instance-based, which I don't think are pertinent
 * to us.
 */
public class PythonWSGIAdapter extends WSGIAdapter {

    protected final File _file;
    protected final JSFileLibrary _lib;

    private long _lastCompile;

    protected final PythonInterpreter _interp;

    protected PyObject _run_with_cgi;     // framework entry point
    protected PyObject _wsgi_appEntry;    // code entry point

    /**
     *  DO basic setup - parse the WSGI framework code and get the functional entry point
     *
     * @param context  app context
     * @param f file we are meant to call  - this is currently irrelevant as the WSGI entry is fixed.
     *          We need to fix this to explore the specified file for the WSGI entry point
     * @param lib current lib for this app
     */
    PythonWSGIAdapter(AppContext context, File f, JSFileLibrary lib) {
        _file = f;
        _lib = lib;

        SiteSystemState ssstate = Python.getSiteSystemState(context, context.getScope());

        PythonInterpreter.initialize(System.getProperties(), new Properties(), new String[0]);

        PySystemState sys = ssstate.getPyState();
        _interp = new PythonInterpreter(null, sys);

        PySystemState oldState = Py.getSystemState();
        try {
            Py.setSystemState(sys);

            // just use the standard wsgiref package that comes w/ python
            
            _interp.exec("import wsgiref.handlers\ndef invoke_wsgi(application):\n    wsgiref.handlers.CGIHandler().run(application)\n");
            _run_with_cgi = _interp.get("invoke_wsgi");

            _getAppCode();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            Py.setSystemState(oldState);
        }
    }

    /**
     *  The actual rendering call - use the framework and the app entry point and stand back!
     *
     * @param env WSGI env map - includes CGI + specified WSGI elements
     * @param stdin input stream
     * @param stdout output stream
     * @param ar request object for this request
     */
    public void handleWSGI(EnvMap env, InputStream stdin, OutputStream stdout, AppRequest ar) {

        // TODO - I'm vague on the setup - need to sit down w/ Ethan and refactor this, CGI and Jxp

        AppContext ac = ar.getContext();

        Scope siteScope = ac.getScope();

        SiteSystemState ss = Python.getSiteSystemState(ac, siteScope);

        PySystemState pyOld = Py.getSystemState();

        ss.flushOld();

        ss.ensurePath(_file.getParent());
        ss.ensurePath(_lib.getRoot().toString());
        ss.ensurePath(_lib.getTopParent().getRoot().toString());

        PyObject globals = ss.globals;
        globals.__setitem__("__file__", Py.newString(_file.toString()));

        PyObject environ = ss.getPyState().getEnviron();

        for (String key : env.keySet()) {

            Object o = env.get(key);

            if (o instanceof PyObject) {
                environ.__setitem__(key.intern(),  (PyObject) o);
            }
            else {
                // Hail Mary, full of grace...
                environ.__setitem__(key.intern(), Py.newString((String) o));
            }
        }
        
        PythonCGIAdapter.CGITLSData cgiosw = new PythonCGIAdapter.CGITLSData(new PyFile(stdin), stdout, null);

        ss.getPyState().stdout = new PythonCGIOutFile();
        ss.getPyState().stdin = new PythonCGIAdapter.PyTLSProxyFile();

        try {
            _getAppCode();   // get latest app code
            Py.setSystemState(ss.getPyState());
            _run_with_cgi.__call__(_wsgi_appEntry);
        }
        catch (IOException e) {
            e.printStackTrace(); // TODO - fix
        } finally {
            cgiosw.unset();
            Py.setSystemState(pyOld);
        }
    }

    public void handleCGI(EnvMap env, InputStream stdin, OutputStream stdout, AppRequest ar) {
        handleWSGI(env, stdin, stdout, ar);
    }

    /**
     *  Refreshes the application code based on modification time.  Will see the internal _wsgi_apEntry
     *  variable (code entry point of the WSGI app)
     * 
     * @throws IOException if problem w/ specified application file
     */
    protected void _getAppCode() throws IOException {

        long lastModified = _file.lastModified();

        if (_lastCompile < lastModified) {
            String code = StreamUtil.readFully(_file);
            _interp.exec(code);
            _wsgi_appEntry = _interp.get("application"); // TODO - let specify in _init.js or app meta and make reloadable
            _lastCompile = lastModified;
        }
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
}
