// JSData.java

package ed.js;

import java.util.*;

import ed.js.func.*;
import ed.js.engine.*;

public class JSDate extends JSObjectBase implements Comparable {

    public static JSFunction _cons = 
        new JSFunctionCalls1(){
            
            public JSObject newOne(){
                return new JSDate();
            }
            
            public Object call( Scope s , Object foo , Object[] args ){
                
                JSObject o = s.getThis();
                if ( o == null )
                    return new JSDate( foo );

                JSDate d = (JSDate)o;
                long l = parse( foo , d._time );
                d._time = l;
                
                return d;
            }
            
            
            protected void init(){
                
                _prototype.set( "getTime" , new JSFunctionCalls0() {
                        public Object call( Scope s , Object foo[] ){
                            return ((JSDate)s.getThis())._time;
                        }
                    } );
            }
            
        };
    
    static long parse( Object o ){
        return parse( o , System.currentTimeMillis() );
    }
    
    static long parse( Object o , long def ){
        if ( o == null )
            return def;
        if ( ! ( o instanceof Number ) )
            return def;
        return ((Number)o).longValue();
    }
    
    public JSDate(){
        this( System.currentTimeMillis() );
    }

    public JSDate( long t ){
        _time = t;
    }

    public JSDate( Object foo ){
        this( parse( foo ) );
    }

    public long getTime(){
        return _time;
    }

    public int getYear(){
        _cal();
        int y = _c.get( Calendar.YEAR );
        if ( y >= 0 && y < 200 )
            return 1900 + y;
        return y;
    }

    public int getMonth(){
        _cal();
        return 1 + _c.get( Calendar.MONTH );
    }

    public int getDay(){
        _cal();
        return _c.get( Calendar.DAY_OF_MONTH );
    }

    public String toString(){
        return new java.util.Date( _time ).toString();
    }

    private void _cal(){
        if ( _c != null )
            return;
        _c = Calendar.getInstance();
        _c.setTimeInMillis( _time );
    }

    public int compareTo( Object o ){
        long t = -1;
        if ( o instanceof JSDate )
            t = ((JSDate)o)._time;
        
        if ( t < 0 )
            return 0;
        
        long diff = _time - t;
        if ( diff == 0 )
            return 0;
        if ( diff < 0 )
            return -1;
        return 1;
    }

    long _time;
    Calendar _c;
}
