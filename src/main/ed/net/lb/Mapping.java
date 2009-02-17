// Mapping.java

/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
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
