// CoreJS.java

package ed.js;

import ed.appserver.*;

/** The corejs module.
 * @expose
 */
public class CoreJS extends Module {

    private static final CoreJS _corejs = new CoreJS();

    private CoreJS(){
        super( "corejs" , "core" , true );
    }

    /** Returns this module.
     * @return This module.
     */
    public static CoreJS get(){
        return _corejs;
    }

}
