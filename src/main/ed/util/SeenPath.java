// SeenPath.java

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

public class SeenPath extends IdentityHashMap {

    public SeenPath(){
        this( false );
    }
    
    public SeenPath( boolean skipWeak ){
        _skipWeak = skipWeak;
    }
    
    public boolean shouldVisit( Object toVisit , Object from ){
        if ( toVisit == null )
            return false;

        if ( dontTraverseSpecial( toVisit ) )
            return false;

        if ( isBadWeak( from ) )
            throw new RuntimeException( "why is a weak thing the from" );

        if ( from == null )
            from = new Unknown();
        
        final Object prev = get( toVisit );
        if ( prev == null ){
            put( toVisit , from );
            return true;
        }

        if ( prev instanceof Unknown ){
            // we want to add some pathing info, but not follow
            put( toVisit , from );
            return false;
        }
        
        return false;
    }
    
    public boolean contains( Object o ){

        if ( dontTraverseSpecial( o ) )
            return true;

        return containsKey( o );
    }

    public void visited( Object toVisit ){
        if ( containsKey( toVisit ) )
            return;
        put( toVisit , new Unknown() );
    }

    public void removeAll( Set objects ){
        for ( Object o : objects )
            remove( o );
    }

    public void pushSpecialDontTraverse( Set s ){
        _specialDontTraverse.add( s );
    }

    public void popSpecialDontTraverse(){
        _specialDontTraverse.remove( _specialDontTraverse.size() - 1 );
    }
    
    boolean dontTraverseSpecial( Object o ){
        
        if ( isBadWeak( o ) )
            return true;
        
        for ( Set s : _specialDontTraverse )
            if ( s.contains( o ) )
                return true;
        
        return false;
    }

    boolean isBadWeak( Object o ){
        return 
            _skipWeak && 
            ( o instanceof WeakBag || 
              o instanceof java.lang.ref.WeakReference || 
              o instanceof WeakHashMap );
    }

    public List path( final Object from , final Object to ){

        if ( ! containsKey( to ) )
            throw new RuntimeException( "the object you want to find doesn't exist" );
        
        Object cur = to;

        List path = new ArrayList();
        while ( true ){
            
            Object next = get( cur );
            if ( next == from )
                return path;
            
            if ( next == null || next instanceof Unknown ){
                String msg = "can't find path.  last piece is a : " + cur.getClass().getName();
                msg += " path : ";
                for ( int i=0; i<path.size(); i++ )
                    msg += " " + path.get(i).getClass().getName();
                
                Throwable t = null;

                if ( next instanceof Unknown ) 
                    t = ((Unknown)next)._where;
    
                throw new RuntimeException( msg , t );
            }
            
            for ( int i=0; i<path.size(); i++ )
                if ( path.get(i) == next )
                    throw new RuntimeException( "loop!" );

            path.add( next );
            cur = next;
        }
    }
    
    final boolean _skipWeak;
    final List<Set> _specialDontTraverse = new ArrayList<Set>();

    static class Unknown {

        Unknown(){
            _where = new Throwable( "created here" );
            _where.fillInStackTrace();
        }

        public String toString(){
            return "unkown path";
        }
        
        final Throwable _where;
    }
}
