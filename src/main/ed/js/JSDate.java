// JSData.java

package ed.js;

import ed.js.func.*;

public class JSDate extends JSObjectBase {
    public JSDate(){
        _time = System.currentTimeMillis();
        set( "getTime" , new JSFunctionCalls0(){
                public Object call( ed.js.engine.Scope s, Object foo[] ){
                    return _time;
                }
            } );
    }

    public String toString(){
        return new java.util.Date( _time ).toString();
    }

    long _time;
}
