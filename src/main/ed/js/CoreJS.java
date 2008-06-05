// CoreJS.java

package ed.js;

import java.io.*;

import ed.log.*;
import ed.js.engine.*;
import ed.appserver.*;

public class CoreJS extends Module {
    
    private static final File _root = new File( System.getenv( "COREJS" ) == null ? "/data/corejs" : System.getenv( "COREJS" ) );
    private static final CoreJS _corejs = new CoreJS();

    private CoreJS(){
        super( _root , "core" , true );
    }

    public static CoreJS get(){
        return _corejs;
    }

}
