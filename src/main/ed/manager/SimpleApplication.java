// SimpleApplication.java

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

public class SimpleApplication implements Application {

    public SimpleApplication( File root , String type , String id , String[] commands ){
        _root = root;
        _type = type;
        _id = id;
        _fullId = _type + "." + _id;
        _commands = commands;
        
        _logDir = new File( _root , "logs" );
        _environment = new TreeMap<String,String>();
    }

    public String getFullId(){
        return _fullId;
    }

    public File getExecDir(){
        return _root;
    }
    
    public File getLogDir(){
        return _logDir;
    }

    public String getType(){
        return _type;
    }

    public String getId(){
        return _id;
    }

    public String[] getCommand(){
        return _commands;
    }
    
    public Map<String,String> getEnvironmentVariables(){
        return _environment;
    }

    public boolean restart( int exitCode ){
        return exitCode != 0;
    }

    public boolean gotOutputLine( String line ){
        return true;
    }

    public boolean gotErrorLine( String line ){
        return true;
    }

    public long timeToShutDown(){
        return 1000 * 2;
    }
    
    public boolean sameConfig( Application other ){
        if ( ! ( other instanceof SimpleApplication ) ){
            return false;
        }

        SimpleApplication sa = (SimpleApplication)other;
        
        return 
            _fullId.equals( sa._fullId ) &&
            _root.equals( sa._root ) &&
            _logDir.equals( sa._logDir ) &&
            Arrays.toString( _commands ).equals( Arrays.toString( sa._commands ) ) &&
            _environment.toString().equals( sa._environment.toString() );
    }

    public int hashCode(){
        return _fullId.hashCode();
    }
    
    public boolean equals( Object o ){
        return ((SimpleApplication)o)._fullId.equals( _fullId );
    }

    protected final File _root;
    protected final File _logDir;

    protected final String _type;
    protected final String _id;
    protected final String _fullId;
    
    protected final String[] _commands;
    
    protected final Map<String,String> _environment;
    
}
