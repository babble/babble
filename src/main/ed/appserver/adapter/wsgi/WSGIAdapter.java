// WSGIAdapter.java

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
