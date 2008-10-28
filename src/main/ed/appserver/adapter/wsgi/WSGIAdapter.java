package ed.appserver.adapter.wsgi;

import ed.appserver.adapter.cgi.CGIAdapter;
import ed.appserver.adapter.cgi.EnvMap;
import ed.appserver.AppRequest;

import java.io.InputStream;
import java.io.OutputStream;


/**
 * First pass at WSGI adapter.  WSGI mandates as much CGI
 * as possible, so simply leverage the CGI work to date.
 */
public abstract class WSGIAdapter extends CGIAdapter {

    public abstract void handleWSGI(EnvMap env, InputStream stdin, OutputStream stdout, AppRequest ar);
}
