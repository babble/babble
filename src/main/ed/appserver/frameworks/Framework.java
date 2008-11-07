// Framework.java

package ed.appserver.frameworks;

import ed.appserver.*;

public abstract class Framework {

    public abstract void install( AppContext context );

    public static Framework forName( String name ){
        return null;
    }

}
