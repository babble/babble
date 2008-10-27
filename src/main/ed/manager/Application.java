// Application.java

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
    public boolean gotOutputLine( String line );

    /**
     * @return true if it should be logged
     */
    public boolean gotErrorLine( String line );

    public boolean sameConfig( Application other );
}
