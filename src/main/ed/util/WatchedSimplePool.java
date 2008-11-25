// WatchedSimplePool.java

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

package ed.util;

import java.util.*;

import ed.net.httpserver.*;

public abstract class WatchedSimplePool<T> extends SimplePool<T> {

    public WatchedSimplePool( String name , int maxToKeep , int maxTotal ){
        super( name , maxToKeep , maxTotal );

        if ( _pools.put( name , this ) != null )
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

    private static final Map<String,WatchedSimplePool> _pools = new TreeMap<String,WatchedSimplePool>();
}


