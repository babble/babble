package ed.lang.python;

import java.util.*;
import org.python.modules.Setup;
import ed.security.*;

public class SafeJavaClasses {
    public static final Set<String> safeClassNames = new HashSet<String>();
    static {
        // FIXME: does this get to see the modules added by python.modules.builtin?
        String[] builtinModules = Setup.builtinModules;
        for(int i = 0; i < builtinModules.length; ++i){
            String module = builtinModules[i];
            int colon = module.indexOf(':');
            if( colon != -1 ){
                module = module.substring( colon + 1 );
                if( module.equals("null") ) continue;
            }

            safeClassNames.add( module );
        }

    }

    public static boolean isSafeClass( String className ){
        if( safeClassNames.contains( className ) ) return true;
        return Security.nonSecureCanAccessClass( className );
    }
}
