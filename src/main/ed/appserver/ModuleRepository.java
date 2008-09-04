// ModuleRepository.java

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

package ed.appserver;

import java.io.*;
import java.net.*;
import java.util.*;

import ed.io.*;
import ed.js.*;
import ed.log.*;
import ed.util.*;

public abstract class ModuleRepository {
    
    static final long RESYNC_TIME = 1000 * 60 * 5;
    static final Logger LOGGER = Logger.getLogger( "module-repository" );
    static final File _cacheDir = new File( "logs/module-cache/" );
    static {
        _cacheDir.mkdirs();
    }

    protected ModuleRepository( String location ){
        _location = location;
        _cacheFile = new File( _cacheDir , _location.replaceAll( "[^\\w]+" , "_" ) );
    }

    protected abstract Map<String,ModuleConfig> getNewConfig();

    public boolean hasModule( String name ){
        sync();
        return _configs.containsKey( name );
    }

    public ModuleConfig getConfig( String name ){
        sync();        
        return _configs.get( name );
    }
    
    public Set<String> getAllNames(){
        sync();
        return _configs.keySet();
    }

    public void forceUpdate(){
        _lastSyncTime = 0;
    }

    void sync(){
        if ( System.currentTimeMillis() < _lastSyncTime + RESYNC_TIME )
            return;
        
        // this is so that if we already have data - don't block waiting for new config
        if ( _lastSyncTime > 0 )
            _lastSyncTime = System.currentTimeMillis();
        
        synchronized( _location ){
            if ( System.currentTimeMillis() + RESYNC_TIME < _lastSyncTime )
                return;
            
            Map<String,ModuleConfig> newConfig = null;
            try {
                newConfig = getNewConfig();
            }
            catch ( Exception e ){
                LOGGER.error( "couldn't get new config from [" + _location + "] - will try using cached config " , e );
                if ( _lastSyncTime == 0 ){
                    try {
                        newConfig = readFromFile( _cacheFile );
                    }
                    catch ( IOException ioe ){
                        LOGGER.error( "tried failing over to cache file [" + _cacheFile + "] but couldn't read" , ioe );
                    }
                    
                    if ( newConfig == null )
                        throw new RuntimeException( "can't load config [" + _location + "] and need to start up"  , e );
                }
            }
            
            if ( newConfig == null )
                throw new RuntimeException( "how could newConfig be null..." );

            _configs = Collections.synchronizedMap( newConfig );
            try {
                writeToFile( _cacheFile );
            }
            catch ( IOException ioe ){
                LOGGER.info( "couldn't write to cache file [" + _location + "] [" + _cacheFile + "]" , ioe );
            }
            
            _lastSyncTime = System.currentTimeMillis();
        }
        
    }

    public JSObject toJSObject(){
        JSObjectBase o = new JSObjectBase();
        for ( String name : _configs.keySet() ){
            o.set( name , _configs.get( name ).toJSObject() );
        }
        return o;
    }

    public String toString(){
        return "ModuleRepository [" + _location + "]";
    }

    private Map<String,ModuleConfig> readFromFile( File f )
        throws IOException {

        if ( ! f.exists() )
            return null;

        String str = StreamUtil.readFully( f );
        JSObject o = (JSObject)JSON.parse( str );

        Map<String,ModuleConfig> m = new StringMap<ModuleConfig>();
        
        for ( String s : o.keySet() ){
            m.put( s , new ModuleConfig( s , (JSObject)(o.get( s ) ) ) );
        }

        return m;
    }

    private void writeToFile( File f )
        throws IOException {
        final String str = JSON.serialize( toJSObject() );
        FileOutputStream fout = new FileOutputStream( f );
        fout.write( str.getBytes() );
        fout.close();
    }

    final String _location;
    final File _cacheFile;
    private long _lastSyncTime = 0;
    private Map<String,ModuleConfig> _configs;

    public static class Web extends ModuleRepository {
        public Web( String url ){
            super( url );
            _url = url;
        }
        
        protected Map<String,ModuleConfig> getNewConfig(){
            Object list = go( "_list" );
            if ( ! ( list instanceof JSArray ) )
                throw new RuntimeException( "result of _list operation has to be an array, not [" + list + "]" );

            Map<String,ModuleConfig> m = new StringMap<ModuleConfig>();
            for ( Object nameObject : (JSArray)list ){
                String name = nameObject.toString();
                try {
                    m.put( name , new ModuleConfig( name , (JSObject)(go( name ) ) ) );
                }
                catch ( Exception e ){
                    throw new RuntimeException( "couldn't get config for [" + name + "]" , e );
                }
            }
            
            return m;
        }
        
        Object go( String command ){
            final String url = _url + command;
            
            XMLHttpRequest req = new XMLHttpRequest( "GET" , url , false );
            try {
                req.send();
            }
            catch ( IOException ioe ){
                throw new RuntimeException( "error downloading [" + url + "]" , ioe );
            }
            return req.getJSON();
        }
        
        final String _url;
    }

}
