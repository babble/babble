// CoreJS.java

package ed.js;

import java.io.*;

import ed.js.engine.*;
import ed.appserver.*;

public class CoreJS extends JSFileLibrary {
    
    private static final File _root = new File( "/data/corejs" );

    public static String getDefaultRoot(){
        return _root.toString();
    }

    public CoreJS( Scope scope ){
        super( _root , "core" , null , scope , true );
    }

    public CoreJS( AppContext context ){
        super( _root , "core" , context , null , true );
    }
}
