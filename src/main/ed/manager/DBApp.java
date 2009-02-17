// DBApp.java

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

import ed.util.*;

public class DBApp extends SimpleApplication {

    DBApp( String id ){
        this( id , new String[0] );
    }

    DBApp( String id , Map<String,String> options ){
        this( id , _optionsToArgs( options ) );
    }

    DBApp( String id , String args[] ){
        super( _findDatabase() , "db" , id , _fixCommands( args ) );

        String dbpath = "/data/db/";
        int port = ed.db.DBPort.PORT;

        for ( int i=0; i<_commands.length-1; i++ ){
            if ( _commands[i].equals( "--dbpath" ) )
                dbpath = _commands[i+1];
            
            if ( _commands[i].equals( "--port" ) )
                port = Integer.parseInt( _commands[i+1] );
        }
        
        if ( dbpath.startsWith( "/" ) )
            ( new File( dbpath ) ).mkdirs();
        else 
            ( new File( getExecDir() , dbpath ) ).mkdirs();

        _port = port;

    }

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

    static String[] _optionsToArgs( Map<String,String> m ){
        if ( m == null || m.size() == 0 )
            return null;

        List<String> args = new ArrayList<String>();

        for ( String c : _configs ){
            if ( m.containsKey( c ) ){
                args.add( "--" + c );
                args.add( m.get( c ) );
            }
        }

        for ( String b : _booleans ){
            if ( m.containsKey( b ) && StringParseUtil.parseBoolean( m.get(b) , false ) ){
                args.add( "--" + b );
            }
        }

        if ( m.containsKey( "pairwith" ) ){
            args.add( "--pairwith" );
            args.add( m.get( "pairwith" ) );
            args.add( "-" );
        }
        
        String[] arr = new String[args.size()];
        return args.toArray( arr );        
    }

    final int _port;
    final static String[] _configs = new String[]{ "port" , "dbpath" , "appsrvpath" , "source"  };
    final static String[] _booleans = new String[]{ "master" , "slave" , "nocursors" , "nojni" };
    
    public static void main( String args[] )
        throws InterruptedException {
        OneTimeApplicationFactory factory = new OneTimeApplicationFactory( new DBApp( "play" ) );
        Manager m = new Manager( factory );
        m.start();
        m.join();
    }
    
}
