// ConfigurableApplicationFactory.java

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

package ed.manager;

import java.io.*;
import java.util.*;

import ed.io.*;
import ed.log.*;
import ed.util.*;

public abstract class ConfigurableApplicationFactory implements ApplicationFactory {

    ConfigurableApplicationFactory( long timeBetweenRefresh ){
        _timeBetweenRefresh = timeBetweenRefresh;
    }

    protected abstract SimpleConfig getConfig()
        throws IOException;
    
    public String textView(){
        if ( _lastConfig == null )
            return "not run yet";
        return _lastConfig.toString();
    }

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
        
        _lastConfig = config;

        List<Application> apps = new ArrayList<Application>();
        
        for ( String id : config.getNames( "db" ) )
            apps.add( new DBApp( id , config.getMap( "db" , id ) ) );
        
        for ( String id : config.getNames( "appserver" ) )
            apps.add( new AppServerApp( id , config.getMap( "appserver" , id ) ) );

        for ( String id : config.getNames( "lb" ) )
            apps.add( new LBApp( id , config.getMap( "lb" , id ) ) );

        for ( String id : config.getNames( "java" ) ){
            
            Map<String,String> options = config.getMap( "java" , id );
            

            apps.add( new JavaApplication( "java" , id , 
                                           options.get( "class" ) , 
                                           StringParseUtil.parseIfInt( options.get( "memory" ) , -1 ) ,
                                           SysExec.fix( options.get( "args" ) ) , 
                                           SysExec.fix( options.get( "jvmArgs" ) ) , 
                                           StringParseUtil.parseBoolean( options.get( "gc" ) , false )
                                           )
                      );
            
        }

        _previousApps = apps;
        return apps;
    }
    
    public long timeBetweenRefresh(){
        return _timeBetweenRefresh;
    }

    public boolean runGridApplication(){
        return false;
    }
    
    final long _timeBetweenRefresh;
    private List<Application> _previousApps;
    private SimpleConfig _lastConfig;

    private static final Logger _logger = Logger.getLogger( "appFactory" );
}


