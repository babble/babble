// WatchedSimplePool.java

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

package ed.util;

import java.util.*;

import ed.net.httpserver.*;

public abstract class WatchedSimplePool<T> extends SimplePool<T> {

    public WatchedSimplePool( String name , int maxToKeep , int maxTotal ){
        super( _fixName( name ) , maxToKeep , maxTotal );

        if ( _pools.put( _name , this ) != null )
            throw new RuntimeException( "already had a WatchedSimplePool for [" + name + "]" );
        
        
    }

    /**
     * @return approximate size in bytes
     */
    protected abstract long memSize( T t );
    
    final long memSize(){
        long size = 0;
        
        for ( Iterator<T> i = getAll() ; i.hasNext(); ){
            T t = i.next();
            size += memSize( t );
        }

        return size;
    }

    public static class WebView extends HttpMonitor {
        public WebView(){
            super( "watchedPools" );
        }
        
        public void handle( MonitorRequest request ){
            request.startData( "pools" , "total in memory" , "in use" , "ever created" , "total size (mb)" );

            for ( WatchedSimplePool p : _pools.values() ){
                request.addData( p._name , p.total() , p.inUse() , p.everCreated() , ed.js.JSMath.sigFig( (double)p.memSize() / ( 1024.0 * 1024.0 ) , 5 ) );
            }

            request.endData();
        }
        
    }

    static final String _fixName( String name ){
        if ( _pools.containsKey( name ) )
            return _fixName( name + "X" );
        return name;
    }

    private static final Map<String,WatchedSimplePool> _pools = new TreeMap<String,WatchedSimplePool>();
}


