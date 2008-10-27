// ConfigurableApplicationFactory.java

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

package ed.manager;

import java.io.*;
import java.util.*;

import ed.log.*;
import ed.util.*;

public abstract class ConfigurableApplicationFactory implements ApplicationFactory {

    ConfigurableApplicationFactory( long timeBetweenRefresh ){
        _timeBetweenRefresh = timeBetweenRefresh;
    }

    protected abstract SimpleConfig getConfig()
        throws IOException;
    
    public List<Application> getApplications(){
        SimpleConfig config = null;
        try {
            config = getConfig();
        }
        catch ( Exception e ){
            _logger.error( "can't get config", e );
            if ( _previousApps == null )
                throw new RuntimeException( "can't load config and don't have an old copy" , e );
            return _previousApps;
        }
        
        if ( config == null )
            throw new RuntimeException( "getConfig() returned null, not allowed" );

        List<Application> apps = new ArrayList<Application>();
        
        for ( String id : config.getNames( "db" ) )
            apps.add( new DBApp( id , config.getMap( "db" , id ) ) );

        _previousApps = apps;
        return apps;
    }
    
    public long timeBetweenRefresh(){
        return _timeBetweenRefresh;
    }

    final long _timeBetweenRefresh;
    private List<Application> _previousApps;

    private static final Logger _logger = Logger.getLogger( "appFactory" );
}


