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

package ed.lang.cgi;

import ed.js.func.JSFunctionCalls0;
import ed.js.engine.Scope;
import ed.js.JSFunction;
import ed.net.httpserver.HttpRequest;
import ed.net.httpserver.HttpResponse;
import ed.appserver.jxp.ServletWriter;
import ed.appserver.JSFileLibrary;

import java.util.Enumeration;

/**
 *  Utility class that invokes a Babble-supported
 *  script in a CGI environment.
 */
public class CGIGateway extends JSFunctionCalls0 {

    protected final String _script;
    protected final JSFileLibrary _lib;

    public CGIGateway(String script, JSFileLibrary lib) {
        _script = script;
        _lib = lib;
    }

    /**
     *  Called on script invocation (e.g. JxpServlet line 39)
     *
     *  Creates a CGI dictionary and invokes the specified script with
     *  it as an additional parameter.  Depends on target script on
     *  being able to do CGI - needs to place this dictionary in the
     *  right place for the given langage, and process output e.g.
     *   ed.lang.python.PyCGIOutputHandler
     * 
     * @param scope scope for call
     * @param extra stuff
     * @return whatever the target script returns
     */
    public Object call(Scope scope, Object[] extra) {

        HttpRequest req = (HttpRequest) extra[0];
        HttpResponse resp = (HttpResponse) extra[1];
        ServletWriter writer = (ServletWriter) extra[2];

        JSFunction f = _lib.getFunction(_script);

        EnvMap cgiEnv = makeCGIDict(req);

        return f.call(scope, req, resp, writer, cgiEnv);
    }

    /**
     *  Parses the request into a map
     *
     *  Copied from Jetty's CGI servlet. Kudos to Greg and Jan!
     *  Following is under the Apache License
     *
     *  Lots of work needs to be done here
     *
     * @param req request object
     * @return map CGI map
     */
    public EnvMap makeCGIDict(HttpRequest req) {

        int len=req.getContentLength();

        if (len<0) {
            len=0;
        }

        EnvMap env=new EnvMap();

        // these ones are from "The WWW Common Gateway Interface Version 1.1"
        // look at :
        // http://Web.Golux.Com/coar/cgi/draft-coar-cgi-v11-03-clean.html#6.1.1

        env.set("AUTH_TYPE",req.getAuthType());
        env.set("CONTENT_LENGTH",Integer.toString(len));
        env.set("CONTENT_TYPE",req.getContentType());
        env.set("GATEWAY_INTERFACE","CGI/1.1");

        env.set("PATH_INFO","/");  // TODO - fix

        env.set("PATH_TRANSLATED","/");   // TODO - fix
        env.set("QUERY_STRING",req.getQueryString());
        env.set("REMOTE_ADDR",req.getRemoteAddr());
        env.set("REMOTE_HOST",req.getRemoteHost());

        // The identity information reported about the connection by a
        // RFC 1413 [11] request to the remote agent, if
        // available. Servers MAY choose not to support this feature, or
        // not to request the data for efficiency reasons.
        // "REMOTE_IDENT" => "NYI"
        env.set("REMOTE_USER",req.getRemoteUser());
        env.set("REQUEST_METHOD",req.getMethod());
        env.set("SCRIPT_NAME","");       // TODO - fix

        env.set("SCRIPT_FILENAME","###FIXME2###");   // TODO -fix

        env.set("SERVER_NAME",req.getServerName());
        env.set("SERVER_PORT",Integer.toString(req.getServerPort()));
        env.set("SERVER_PROTOCOL",req.getProtocol());
        env.set("SERVER_SOFTWARE","Development/1.0");

        Enumeration enm=req.getHeaderNames();

        while (enm.hasMoreElements())
        {
            String name=(String)enm.nextElement();
            String value=req.getHeader(name);
            env.set("HTTP_"+name.toUpperCase().replace('-','_'),value);
        }

        // these extra ones were from printenv on www.dev.nomura.co.uk
        env.set("HTTPS",(req.isSecure()?"ON":"OFF"));

        // "DOCUMENT_ROOT" => root + "/docs",
        // "SERVER_URL" => "NYI - http://us0245",
        // "TZ" => System.getProperty("user.timezone"),

        // are we meant to decode args here ? or does the script get them
        // via PATH_INFO ? if we are, they should be decoded and passed
        // into exec here...

        return env;
    }
}
