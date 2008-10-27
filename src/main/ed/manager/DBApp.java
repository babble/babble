// DBApp.java

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

public class DBApp extends SimpleApplication {

    static String[] _dbSearchPath = new String[]{ 
        "../p/" , "../mongo" ,
        System.getProperty( "user.home" ) + "/p/" , 
        System.getProperty( "user.home" ) + "/mongo/" , 
    };

    static File _findDatabase(){
        for ( String path : _dbSearchPath ){
            File f = new File( path );
            if ( f.exists() )
                return f;
        }
        
        throw new RuntimeException( "can't find db" );
    }

    static String[] _fixCommands( String args[] ){
        List<String> commands = new ArrayList<String>();
        commands.add( "./db/db" );

        if ( args == null || args.length == 0 )
            commands.add( "run" );
        else 
            for ( String a : args )
                commands.add( a );

        String[] arr = new String[commands.size()];
        return commands.toArray( arr );
    }

    DBApp( String id , String args[] ){
        super( _findDatabase() , "db" , id , _fixCommands( args ) );
    }
    
    public static void main( String args[] )
        throws InterruptedException {
        OneTimeApplicationFactory factory = new OneTimeApplicationFactory( new DBApp( "play" , null ) );
        Manager m = new Manager( factory );
        m.start();
        m.join();
    }
    
}
