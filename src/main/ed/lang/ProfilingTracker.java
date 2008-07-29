// ProfilingTracker.java

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

import ed.js.*;

public class ProfilingTracker {

    public static void tlGotTime( String name , long time ){
        tlGotTime( name , time , null );
    }

    public static void tlGotTime( String name , long time , Object last ){
        ProfilingTracker pt = getThreadLocal();
        if ( pt == null )
            return;
        pt.gotTime( name , time , last );
    }

    //

    public ProfilingTracker( String name ){
        _name = name;
    }

    public void makeThreadLocal(){
        _threadLocal.set( this );
    }

    public static ProfilingTracker getThreadLocal(){
        return _threadLocal.get();
    }

    public static void resetThreadLocal(){
        _threadLocal.set( null );
    }

    public void gotTime( String name , long time ){
        gotTime( name , time , null );
    }

    public void gotTime( String name , long time , Object last ){
        _root.inc( name , time , last );
    }
    
    public void push( String name ){}
    public void pop(){}

    class Frame {
        
        Frame( String name ){
            _name = name;
        }

        void inc( String name , long t , Object last ){
            inc( t );
            
            if ( name != null )
                _children.inc( name , t , last );
            else if ( last != null )
                _children.inc( null , t , last );
                
        }

        void inc( long t ){
            _time += t;
            _num++;
        }
        
        void toString( StringBuilder buf , int level , String spacer , String eol ){
            for ( int i=0; i<level; i++ )
                buf.append( spacer );
            buf.append( _name ).append( " num:" ).append( _num ).append( " total:" ).append( _time ).append( eol );
            _children.toString( buf , level + 1 , spacer , eol );
        }

        final String _name;
        final FrameSet _children = new FrameSet();
        
        long _time = 0;
        int _num;
    }
    
    class FrameSet extends HashMap<String,Frame> {
        
        void inc( String name , long t , Object last ){
            
            String next = null;

            if ( name == null && last != null ){
                name = serialize( last );
                last = null;
            }
            else {
                int idx = name.indexOf( "." );
                if ( idx > 0 ){
                    next = name.substring( idx + 1 );
                    name = name.substring( 0 , idx );
                }
            }

            Frame f = get( name );
            if ( f == null ){
                f = new Frame( name );
                put( name , f );
            }
            f.inc( next , t , last );
        }
        
        public StringBuilder toString( StringBuilder buf , int level , String spacer , String eol ){
            for ( Frame f : values() ){
                f.toString( buf , level , spacer , eol );
            }
            return buf;
        }
    }
    
    String serialize( Object o ){
        String s = JSON.serialize( o );
        s = s.replaceAll( "[\r\n\\s]+" , " " ).replace( '\\' , ' ' );
        return s;
    }

    public String toString( String spacer , String eol ){
        StringBuilder buf = new StringBuilder();
        buf.append( _name ).append( eol );
        _root.toString( buf , 1 , spacer , eol );
        return buf.toString();
    }

    public String toHtml(){
        return toString( "&nbsp;&nbsp;" , "<br>" );
    }

    public String toString(){
        return toString( "  " , "\n" );
    }

    final String _name;
    private final FrameSet _root = new FrameSet();
    

    private static final ThreadLocal<ProfilingTracker> _threadLocal = new ThreadLocal<ProfilingTracker>();
    
}
