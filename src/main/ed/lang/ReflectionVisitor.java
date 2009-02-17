// ReflectionVisitor.java


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

package ed.lang;

import java.util.*;
import java.lang.reflect.*;

import ed.util.*;

public interface ReflectionVisitor {

    /**
     * @return whether or not we should traverse into this object
     */
    public boolean visit( Object o , Class c );

    /**
     * @return whether or not to follow the link from the object th the field
     */
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

    public static class ShortestPathFinder extends Reachable {

        ShortestPathFinder( Object ... toFind ){
            super( false );
            _toFind = toFind;
        }

        public boolean visit( Object o , Class c ){
            for ( int i=0; i<_toFind.length; i++ )
                if ( o == _toFind[i] )
                    throw new RuntimeException( "yay!" );

            return super.visit( o , c );
        }

        final Object[] _toFind;
    }
}

