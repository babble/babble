// PrintBuffer.java

package ed.js;

public class PrintBuffer extends ed.js.func.JSFunctionCalls1 {
    
    public Object call( ed.js.engine.Scope s , Object foo , Object extra[] ){
        _buf.append( foo );
        return null;
    }

    public String toString(){
        return _buf.toString();
    }

    final StringBuilder _buf = new StringBuilder();
}
