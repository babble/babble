// ReflectionVisitor.java


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

package ed.lang;

import java.util.*;
import java.lang.reflect.*;

import ed.util.*;

public interface ReflectionVisitor {
    
    public boolean visit( Object o , Class c );
    public boolean follow( Object o , Class c , Field f );


    public static class Reachable implements ReflectionVisitor {

        public Reachable(){
            this( false );
        }
        
        public Reachable( boolean followWeakRefs ){
            _followWeakRefs = followWeakRefs;
        }
        
        public boolean visit( Object o , Class c ){
            if ( _seen.contains( o ) )
                return false;
            
            _seen.add( o );
            
            if ( ! _seenClasses.contains( c ) ){
                _seenClasses.add( c );
                System.out.println( c.getName() );
            }
            
            return true;
        }
        
        public boolean follow( Object o , Class c , Field f ){
            if ( o instanceof Number ||
                 o instanceof Boolean || 
                 o instanceof String || 
                 o instanceof Byte || 
                 o instanceof Class || 
                 o instanceof ClassLoader || 
                 o instanceof Character )
                return false;
            
            if ( ! _followWeakRefs && o instanceof java.lang.ref.WeakReference )
                return false;
            
            if ( _stoppers.contains( c ) )
                return false;

            return true;
        }

        public int seenSize(){
            return _seen.size();
        }
        
        public void addStopper( Class c ){
            _stoppers.add( c );
        }
        
        
        final boolean _followWeakRefs;
        final IdentitySet _seen = new IdentitySet();
        final Set<Class> _seenClasses = new HashSet<Class>();

        final Set<Class> _stoppers = new HashSet<Class>();
    }
}

