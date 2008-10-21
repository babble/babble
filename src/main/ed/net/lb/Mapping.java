// Mapping.java

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

package ed.net.lb;

import java.net.*;
import java.util.*;

import ed.net.*;
import ed.net.httpserver.*;

public interface Mapping {

    public Environment getEnvironment( HttpRequest request );
    public String getPool( Environment e );
    public String getPool( HttpRequest request );
    public List<InetSocketAddress> getAddressesForPool( String poolName );

    public List<String> getPools();

    public String toFileConfig();

    public boolean reject( HttpRequest request );
    
}
