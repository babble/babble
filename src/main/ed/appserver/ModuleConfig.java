// ModuleConfig.java

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
