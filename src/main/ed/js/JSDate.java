// JSData.java

package ed.js;

import ed.js.func.*;

public class JSDate extends JSObject {
    public JSDate(){
        _time = System.currentTimeMillis();
        set( "getTime" , new JSFunctionCalls0(){
                public Object call( ed.js.engine.Scope s, Object foo[] ){
                    return _time;
                }
            } );
    }

    long _time;
}
