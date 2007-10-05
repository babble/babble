// JSString.java

package ed.js;

public class JSString extends JSObject {
    public JSString( String s ){
        _s = s;
        set( "length" , Integer.valueOf( _s.length() ) );
        set( "charCodeAt" , new JSFunction(1) {
                public Object call( ed.js.engine.Scope s , Object o ){
                    int idx = ((Number)o).intValue();
                    return Integer.valueOf( _s.charAt( idx ) );
                }
            } );
    }
    
    private String _s;
}
