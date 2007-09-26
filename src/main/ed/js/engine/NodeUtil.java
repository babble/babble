// NodeUtil.java

package ed.js.engine;

import org.mozilla.javascript.*;

public class NodeUtil {

    static boolean hasString( Node n ){
        return n.getClass().getName().indexOf( "StringNode" ) >= 0;
    }

}
