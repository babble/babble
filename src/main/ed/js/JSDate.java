// JSData.java

package ed.js;

import ed.js.func.*;
import ed.js.engine.*;

public class JSDate extends JSObjectBase {

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
                
                System.out.println( "init" );
                
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

    public String toString(){
        return new java.util.Date( _time ).toString();
    }

    long _time;
}
