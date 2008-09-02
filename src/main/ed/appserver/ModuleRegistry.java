// ModuleRegistry.java

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
import java.util.*;

import ed.util.*;

public class ModuleRegistry {

    public static ModuleRegistry getARegistry(){
        return DEFAULT;
    }

    private static final ModuleRegistry DEFAULT;
    static {
        DEFAULT = new ModuleRegistry( null );
        DEFAULT.addRepository( new ModuleRepository.Web( Config.get().getProperty( "moduleRoot" , "http://modules.10gen.com/api/" ) ) );
    }
    
    public ModuleRegistry( ModuleRegistry parent ){
        _parent = parent;
    }

    public ModuleConfig getConfig( String name ){
        for ( ModuleRepository mr : _repositories )
            if ( mr.hasModule( name ) ){
                ModuleConfig config = mr.getConfig( name );
                if ( config == null )
                    throw new RuntimeException( "why did a [" + mr + "] say it had a config for [" + name + "] when it didn't" );
                return config;
            }
        
        if ( _parent != null )
            return _parent.getConfig( name );
        
        System.out.println( "can't find config for module [" + name + "] from " + _repositories );
        return null;
    }

    public void addRepository( ModuleRepository repository ){
        if ( _locked )
            throw new RuntimeException( "this ModuleRegistry locked" );
        _repositories.add( repository );
    }
    
    public void lock(){
        _locked = true;
    }

    private boolean _locked = false;

    final List<ModuleRepository> _repositories = new LinkedList<ModuleRepository>();
    final ModuleRegistry _parent;
}
