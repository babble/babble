// Application.java

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

public interface Application {

    public File getExecDir();
    public File getLogDir();

    /**
     * basic name (db,appserver,lb)
     */
    public String getType();

    /**
     * unique identifier within type (prod1,dev1)
     */
    public String getId();

    /**
     * usually <type>.<id>
     */
    public String getFullId();


    public String[] getCommand();
    public Map<String,String> getEnvironmentVariables();
    
    public boolean restart( int exitCode );
    
    /**
     * @return true if it should be logged
     */
    public boolean gotOutputLine( String line ) throws RestartApp;

    /**
     * @return true if it should be logged
     */
    public boolean gotErrorLine( String line ) throws RestartApp ;

    public boolean sameConfig( Application other );


    public long timeToShutDown();

    public static class RestartApp extends Exception {
        RestartApp( String why ){
            super( "AppRestart : " + why );
            _why = why;
        }

        final String _why;
    }

}
