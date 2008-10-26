// JavaApplication.java

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

public class JavaApplication extends SimpleApplication {

    public final static String JAVA = "./runLight.bash";

    public JavaApplication( String type , String id , String className ){
        this( type , id , className , -1 , null , null );
    }

    public JavaApplication( String type , String id , String className , int maxMemory , String[] args , String[] jvmArgs ){
        super( new File( "." ) , type , id , _getCommands( type , className , args , jvmArgs , maxMemory , true ) );
    }

    public boolean gotOutputLine( String line ){
        return ! isGCLine( line );
    }

    public boolean gotErrorLine( String line ){
        return ! isGCLine( line );
    }

    boolean isGCLine( String line ){
        // TODO:
        return false;
    }

    static String[] _getCommands( String type , String className , String[] args , String[] jvmArgs , int maxMemory , boolean gc ){
        
        List<String> commands = new ArrayList<String>();
        
        commands.add( JAVA );
        if ( gc ){ 
            commands.add( "-verbose:gc" );
            commands.add( "-XX:+PrintGCDetails" );
            commands.add( "-XX:+PrintGCTimeStamps" );
        }
        
        if ( maxMemory > 0 )
            commands.add( "-Xmx" + maxMemory + "m" );
        
        if ( jvmArgs != null )
            for ( String s : jvmArgs )
                commands.add( _quote( s ) );

        commands.add( className );
        
        if ( args != null )
            for ( String s : args )
                commands.add( _quote( s ) );
        
        String[] arr = new String[commands.size()];
        commands.toArray( arr );
        return arr;
    }

    static String _quote( String s ){
        if ( s.contains( " " ) || 
             s.contains( "?" ) || 
             s.contains( "'" ) )
            return "\"" + s + "\"";
        return s;
    }
}
