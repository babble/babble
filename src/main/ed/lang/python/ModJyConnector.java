/*###
  #
  # Copyright 2004-2007 Alan Kennedy.
  #
  # You may contact the copyright holder at this uri:
  #
  # http://www.xhaus.com/contact/modjy
  #
  # The licence under which this code is released is the Apache License v2.0.
  #
  # The terms and conditions of this license are listed in a file contained
  # in the distribution that also contained this file, under the name
  # LICENSE.txt.
  #
  # You may also read a copy of the license at the following web address.
  #
  # http://www.xhaus.com/modjy/LICENSE.txt
  #
  ###*/

package ed.lang.python;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.python.core.*;
import org.python.util.*;
import ed.appserver.*;

public class ModJyConnector extends HttpServlet  {

    private PythonInterpreter interp;
    private HttpServlet modjy_servlet;
    private PyObject py_servlet;
    private Map<File, Long> lastMod;

    public void init ( )
	throws ServletException{
        lastMod = new HashMap<File, Long>();

	Properties props = new Properties();
        // Context parameters
	ServletContext context = getServletContext();
	Enumeration e = context.getInitParameterNames();
	while (e.hasMoreElements()){
	    String name = (String) e.nextElement();
	    props.put(name, context.getInitParameter(name));
	}
        // Servlet parameters
	e = getInitParameterNames();
	while (e.hasMoreElements()){
	    String name = (String) e.nextElement();
	    props.put(name, getInitParameter(name));
	}
        System.out.println("Here's the context " + context.toString() + " " + context.getClass());
        if( ! ( context instanceof AppContext ) ){
            throw new RuntimeException( "not called from an AppContext??" );
        }
        AppContext ac = (AppContext)context;

        try {
            SiteSystemState ssstate = Python.getSiteSystemState( ac , ac.getScope() );
	    PythonInterpreter.initialize(System.getProperties(), props, new String[0]);
            PySystemState sys = ssstate.getPyState();
            interp = new PythonInterpreter( null , sys );
	    // Ouch. Why doesn't tomcat set the classpath?
	    if (props.get("modjy_jar.location") != null)
		sys.path.append(new PyString((String)props.get("modjy_jar.location")));
	    else
		sys.path.append(new PyString( "include/modjy.jar" ) );

            PySystemState oldState = Py.getSystemState();
	    try{
                Py.setSystemState(sys);
                interp.exec("from modjy import modjy_servlet");
                py_servlet = ((PyClass)interp.get("modjy_servlet")).__call__();
                Object temp = py_servlet.__tojava__(HttpServlet.class);
                if (temp == Py.NoConversion)
                    throw new ServletException("Corrupted modjy file: cannot find definition of modjy_servlet class");
                modjy_servlet = (HttpServlet) temp;
                modjy_servlet.init(this);
            }
	    catch (PyException ix){
		throw new ServletException("Unable to import modjy_servlet: do you maybe need to set the 'modjy_jar.location' parameter?");
	    }
            finally {
                Py.setSystemState(oldState);
            }

	}
	catch (PyException pyx){
	    throw new ServletException("Exception creating modjy servlet: " + pyx.toString());
	}
    }

    public boolean needsRefresh( File f ){
        return ! lastMod.get( f ).equals(f.lastModified());
    }

    public void updateLastMod(){
        PyObject dict = ((PyInstance)py_servlet).__dict__;
        PyObject cache = dict.__finditem__("cache".intern());
        if(cache instanceof PyDictionary){
            for(Object o : ((PyDictionary)cache).keySet()){
                if( ! ( o instanceof PyTuple ) ){
                    System.err.println("Cache format changed?? key was " + o.getClass());
                    continue;
                }
                
                String fname = ((PyTuple)o).__finditem__( 0 ).toString();
                File f = new File(fname);
                if( ! lastMod.containsKey( f ) ){
                    // newly inserted into cache
                    lastMod.put( f, f.lastModified() );
                }
            }
        }
    }

    public void service ( HttpServletRequest req, HttpServletResponse resp )
	throws ServletException, IOException {
        PySystemState oldPyState = Py.getSystemState();
	ServletContext context = getServletContext();
        if( ! ( context instanceof AppContext ) ){
            throw new RuntimeException( "can't service from within a " + context.getClass() );
        }
        AppContext ac = (AppContext)context;
        SiteSystemState sss = Python.getSiteSystemState( ac , ac.getScope() );
        System.out.println("Trying to restore context  "+ ac + " " + ac.getScope());
        Set<File> newer = sss.flushOld();
        // XXX: ah, yes, let's poke around in modjy internals
        if(py_servlet instanceof PyInstance){
            PyObject dict = ((PyInstance)py_servlet).__dict__;
            PyObject cache = dict.__finditem__("cache".intern());
            if(cache instanceof PyDictionary){
                for(Object o : ((PyDictionary)cache).keySet()){
                    if( ! ( o instanceof PyTuple ) ){
                        System.err.println("Cache format changed?? key was " + o.getClass());
                        continue;
                    }

                    String fname = ((PyTuple)o).__finditem__( 0 ).toString();
                    File f = new File(fname);
                    if( newer.contains( f.getAbsoluteFile() ) || needsRefresh( f ) ){
                        cache.__delitem__( (PyObject)o );
                        lastMod.remove( f );
                    }
                }
            }
        }

        try {
            Py.setSystemState( sss.getPyState() );

            modjy_servlet.service(req, resp);
            // FIXME: This is kind of a race condition, because the cache gets 
            // filled before the file gets run -- if the file timestamp changes
            // while the file is running, I'm screwed.
            updateLastMod();
        }
        finally {
            Py.setSystemState( oldPyState );
        }
    }

}
