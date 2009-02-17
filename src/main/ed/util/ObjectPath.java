// ObjectPath.java

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

package ed.util;

import java.util.*;

import ed.js.*;
import ed.js.engine.*;

public class ObjectPath extends ArrayList {

    public Object removeLast(){
        return remove( size() -1  );
    }
    
    public void addEndOfPath( Throwable allocated ){
        _ends.add( new EndInfo( allocated ) );
    }
    
    public void foundLoop( Object what ){
        LoopInfo li = new LoopInfo( what );
        String s = li._snapshot.substring( 0 , li._snapshot.length() - 1 );
        for ( LoopInfo old : _loops )
            if ( old._snapshot.startsWith( s ) )
                return;
        _loops.add( li );
    }

    public RuntimeException getDebugException(){
        throw new RuntimeException( "can't find path.  loops : " + _loops + " ends : " + _ends );
    }
    
    public void debug(){
        System.out.println( "loops" );
        for ( LoopInfo l : _loops )
            l.debug();
        
        System.out.println( "ends" );
        for ( EndInfo e : _ends )
            e.debug();

    }

    public static String pathElementsToString( Iterable i ){
        StringBuilder buf = new StringBuilder();
        buf.append( "[" );
        for ( Object o : i ){
            if ( buf.length() > 1 )
                buf.append( ", " );
            buf.append( pathElementToString( o ) );
            buf.append( "]" );
        }
        return buf.toString();
    }
    
    public static String pathElementToString( Object o ){
        if ( o == null )
            return "null";
        
        String s = o.getClass().getName();
        
        if ( o instanceof Scope  
             || o instanceof Number
             || o instanceof JSString
             || o instanceof String )
            s += "(" + o + ")";
        
        if ( o instanceof Collection )
            s += "(size=" + ((Collection)o).size() + ")";


        if ( o instanceof FastStringMap )
            s += "(size=" + ((FastStringMap)o).size() + ")";

        if ( o instanceof JSObjectBase )
            s += "(" + ((JSObjectBase)o)._getName() + ")";
        
        s += ":" + System.identityHashCode( o );

        return s;
    }

    public String toString(){
        StringBuilder buf = new StringBuilder( size() * 20 );
        buf.append( "[" );
        for ( int i=0; i<size(); i++ ){
            if ( i > 0 )
                buf.append( ", " );
            buf.append( pathElementToString( get(i ) ) );
        }
        buf.append( "]" );

        return buf.toString();
    }
    
    class LoopInfo extends Info {
        LoopInfo( Object where ){
            _where = where;
        }
        
        void debug(){
            System.out.println( "\t" + _snapshot + " where:" + pathElementToString( _where ) );
        }

        final Object _where;
    }

    class EndInfo extends Info {
        EndInfo( Throwable t ){
            _t = t;
        }

        void debug(){
            System.out.println( "\t" + _snapshot );
            if ( _t != null )
                _t.printStackTrace();
        }

        final Throwable _t;
    }

    abstract class Info {
        Info(){
            _snapshot = ObjectPath.this.toString();
        }

        abstract void debug();
        
        final String _snapshot;
    }

    final List<LoopInfo> _loops = new ArrayList<LoopInfo>();
    final List<EndInfo> _ends = new ArrayList<EndInfo>();
}
