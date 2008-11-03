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

/**
 * the ModuleRegistry contains information about where to find modules (corejes, external, your own custom modules)
 * and what version of the modules to use.  every context gets its own so you can add search paths for modules
 * or you can just add a module directly with a git url.
 * 
 * obtain by __instance__.getModuleRegistry()
 * 
 * @expose
 * @docmodule system.system.ModuleRegistry
 */
public class ModuleRegistry {

    public static ModuleRegistry getARegistry( AppContext context ){
        if ( context == null )
            return DEFAULT;
        return context.getModuleRegistry();
    }

    public static ModuleRegistry getNewGlobalChild(){
        return new ModuleRegistry( DEFAULT );
    }

    private static final ModuleRegistry DEFAULT;
    static {
        DEFAULT = new ModuleRegistry( null );
        DEFAULT.addRepository( new ModuleRepository.Web( Config.get().getProperty( "moduleRoot" , "http://modules.10gen.com/api/" ) ) );
        DEFAULT.lock();
    }

    public ModuleRegistry( ModuleRegistry parent ){
        _parent = parent;
    }

    /**
     * search all repositories (mine and parents) for a module with the given name
     * @param name examples: corejs, external, wiki
     * @return the ModuleConfig for the given name or null
     */
    public ModuleConfig getConfig( String name ){
        ModuleConfig mc = _specialModules.get( name );
	if ( mc != null )
	    return mc;        

        mc = _find( name );
	if ( mc != null )
	    return mc;
	
	mc = _find( _cleanName( name ) );
	if ( mc != null )
	    return mc;

        if ( _parent != null )
            return _parent.getConfig( name );
        
        return null;
    }

    /**
     * if you don't want to host your own ModuleRepository but would like to make your modules with git
     * you can just add a ModuleConfig directly.  this will also override anything found in a registry
     * @param name name of the module (corejs, wiki)
     * @param config the ModuleConfig you want to use
     */
    public void addModule( String name , ModuleConfig config ){
        _checkLocked();
        _specialModules.put( name , config );
    }

    /**
     * @see other addModule
     * if you don't want to have a full config for a module with versioning, etc...
     * you can just add it is a git url.
     */
    public void addModule( String name , String giturl ){
        addModule( name , new ModuleConfig( name , giturl ) );
    }

    /**
     * add a ModuleRepository to the search path
     */
    public void addRepository( ModuleRepository repository ){
        _checkLocked();
        _repositories.add( repository );
    }
    
    public void lock(){
        _locked = true;
    }

    private void _checkLocked(){
        if ( _locked )
            throw new RuntimeException( "this ModuleRegistry locked" );
    }

    private String _cleanName( String name ){
	final int idx = name.lastIndexOf( "/" );
	if ( idx < 0 )
	    return name;
	return name.substring( idx + 1 );
    }

    private ModuleConfig _find( String name ){
        for ( ModuleRepository mr : _repositories ){
            if ( mr.hasModule( name ) ){
                ModuleConfig config = mr.getConfig( name );
                if ( config == null )
                    throw new RuntimeException( "why did a [" + mr + "] say it had a config for [" + name + "] when it didn't" );
                return config;
            }
	}
	return null;
    }
	
    private boolean _locked = false;

    final List<ModuleRepository> _repositories = new LinkedList<ModuleRepository>();
    final Map<String,ModuleConfig> _specialModules = new TreeMap<String,ModuleConfig>();
    final ModuleRegistry _parent;
}
