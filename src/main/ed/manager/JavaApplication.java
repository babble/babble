// JavaApplication.java

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
import static ed.util.MemUtil.*;

public class JavaApplication extends SimpleApplication {

    public final static String JAVA = "java";

    public JavaApplication( String type , String id , String className ){
        this( type , id , className , -1 , null , null );
    }

    public JavaApplication( String type , String id , String className , int maxMemory , String[] args , String[] jvmArgs ){
        this( type , id , className , maxMemory , args , jvmArgs , true );
    }
    
    public JavaApplication( String type , String id , String className , int maxMemory , List<String> args , List<String> jvmArgs , boolean gc ){
        this( type , id , className , maxMemory , toArray( args ) , toArray( jvmArgs ) , gc );
    }

    public JavaApplication( String type , String id , String className , int maxMemory , String[] args , String[] jvmArgs , boolean gc ){
        super( new File( "." ) , type , id , _getCommands( type , className , args , jvmArgs , maxMemory , gc ) );
        
        String cp = System.getenv( "CLASSPATH" );
        cp += File.pathSeparator + "build";
        _environment.put( "CLASSPATH" , cp );
    }
    
    public boolean gotOutputLine( String line )
        throws RestartApp {
        return ! handleGCLine( line );
    }

    public boolean gotErrorLine( String line )
        throws RestartApp {
        return ! handleGCLine( line );
    }
    
    boolean handleGCLine( String line )
        throws RestartApp {
        if ( ! _gcStream.add( line ) )
            return false;
        
        _gcs.add( line );

        final double fullGCPer = _gcStream.fullGCPercentage();

        if ( fullGCPer > .85 && _gcStream.fullGCsInARow() > 2 ){
            _gcStream.reset();
            System.out.println( "GOING TO RESTART " + this + " BECAUSE OF MEMORY" );
            throw new RestartApp( "too much full gc: " + fullGCPer );
        }

        return true;
    }

    final GCStream _gcStream = new GCStream();
    final CircularList<String> _gcs = new CircularList<String>( 10000 , true );

    static String[] _getCommands( String type , String className , String[] args , String[] jvmArgs , int maxMemory , boolean gc ){

        if ( className == null )
            throw new IllegalArgumentException( "className can't be null" );

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

    public static String[] toArray( List<String> lst ){
        if ( lst == null || lst.size() == 0 )
            return EMPTY_ARR;

        String[] arr = new String[lst.size()];
        return lst.toArray( arr );
    }
    private static final String[] EMPTY_ARR = new String[0];
}
