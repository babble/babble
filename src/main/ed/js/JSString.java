// JSString.java

package ed.js;

public class JSString extends JSObject {
    public JSString( String s ){
        _s = s;
        set( "length" , Integer.valueOf( _s.length() ) );
        set( "charCodeAt" , new JSFunctionCalls1() {
                public Object call( ed.js.engine.Scope s , Object o , Object foo[] ){
                    int idx = ((Number)o).intValue();
                    return Integer.valueOf( _s.charAt( idx ) );
                }
            } );
        set( "charAt" , new JSFunctionCalls1() {
                public Object call( ed.js.engine.Scope s , Object o , Object foo[] ){
                    int idx = ((Number)o).intValue();
                    return _s.substring( idx , idx + 1 );
                }
            } );
    }
    
    public String toString(){
        return _s;
    }
    
    public int hashCode(){
        return _s.hashCode();
    }

    public boolean equals( Object o ){
        return _s.equals( o );
    }

    private String _s;
}
