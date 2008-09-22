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

    public void init ( )
	throws ServletException{

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
	    interp = new PythonInterpreter( null , ssstate.state );
	    // Ouch. Why doesn't tomcat set the classpath?
	    PySystemState sys = Py.getSystemState();
	    if (props.get("modjy_jar.location") != null)
		sys.path.append(new PyString((String)props.get("modjy_jar.location")));
	    else
		sys.path.append(new PyString(getServletContext().getRealPath("/WEB-INF/lib/modjy.jar")));

	    try{
		interp.exec("from modjy import modjy_servlet"); }
	    catch (PyException ix){
		throw new ServletException("Unable to import modjy_servlet: do you maybe need to set the 'modjy_jar.location' parameter?");
	    }

	    PyObject py_servlet = ((PyClass)interp.get("modjy_servlet")).__call__();
	    Object temp = py_servlet.__tojava__(HttpServlet.class);
	    if (temp == Py.NoConversion)
		throw new ServletException("Corrupted modjy file: cannot find definition of modjy_servlet class");
	    modjy_servlet = (HttpServlet) temp;
	    modjy_servlet.init(this);
	}
	catch (PyException pyx){
	    throw new ServletException("Exception creating modjy servlet: " + pyx.toString());
	}
    }

    public void service ( HttpServletRequest req, HttpServletResponse resp )
	throws ServletException, IOException {
	modjy_servlet.service(req, resp);
    }

}
