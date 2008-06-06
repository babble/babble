// CoreJS.java

package ed.js;

import ed.appserver.*;

public class CoreJS extends Module {
    
    private static final CoreJS _corejs = new CoreJS();

    private CoreJS(){
        super( "corejs" , "core" , true );
    }

    public static CoreJS get(){
        return _corejs;
    }

}
