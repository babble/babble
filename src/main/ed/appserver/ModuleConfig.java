// ModuleConfig.java

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

package ed.appserver;

import java.util.*;

import ed.js.*;
import ed.util.*;

public class ModuleConfig {

    private static final String _defaultTagString = Config.get().getProperty( "moduleDefaultTag" , "stable" );
    private static final String[] _defaultTags = _defaultTagString.split( "," );

    public ModuleConfig( String name , JSObject config ){
        _name = name;
        if ( ! name.equalsIgnoreCase( config.get( "name" ).toString() ) )
            throw new RuntimeException( "names don't match [" + name + "] != [" + config.get( "name" ) + "]" );
        
        if ( config.get( "giturl" ) == null )
            throw new RuntimeException( "giturl can't be null" );
        
        _giturl = config.get( "giturl" ).toString();

        Object symlinkObject = config.get( "symlinks" );
        if ( symlinkObject instanceof JSObject ){
            JSObject symlink = (JSObject)symlinkObject;
            for ( String key : symlink.keySet() ){
                Object val = symlink.get( key );
                _aliases.put( key , val == null ? null : val.toString() );
            }
        }
            
    }
    
    public ModuleConfig( String name , String giturl ){
        _name = name;
        _giturl = giturl;
    }

    public void addAlias( String from , String to ){
        _aliases.put( from , to );
    }

    public String getGitUrl(){
        return _giturl;
    }

    public String getDefaultTag(){
        for ( int i=0; i<_defaultTags.length; i++ ){
            String s = followSymLinks( _defaultTags[i] );
            if ( ! s.equalsIgnoreCase( _defaultTags[i] ) )
                return s;
        }
        return "master";
    }

    public String followSymLinks( String tag ){
        while ( _aliases.containsKey( tag ) ){
            String n = _aliases.get( tag );
            if ( n == null )
                throw new RuntimeException( "tag [" + tag + "] has been deprecated" );
            tag = n;
        }
        return tag;
    }

    public JSObject toJSObject(){
        JSObjectBase o = new JSObjectBase();
        o.set( "name" , _name );
        o.set( "giturl" , _giturl );
        
        if ( _aliases.size() > 0 ){
            JSObjectBase s = new JSObjectBase();
            for ( String key : _aliases.keySet() )
                s.set( key , _aliases.get( key ) );
            o.set( "symlinks" , s );
        }
        
        return o;
    }

    public String toString(){
        return "ModuleConfig name [" + _name + "] giturl [" + _giturl + "]";
    }

    final String _name;
    final String _giturl;
    final Map<String,String> _aliases = new StringMap<String>();

}
