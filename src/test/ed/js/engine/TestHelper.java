// TestHelper.java

package ed.js.engine;

public class TestHelper {

    public Object bar( Scope s ){
        return 1;
    }

    public Object foo(){
        return 3;
    }

    public Object foo( Scope s ){
        return s.get( "y" );
    }
}
