// Security.java

package ed.security;

import ed.js.engine.*;

public class Security {

    static final String CORE = Convert.DEFAULT_PACKAGE + "._data_corejs_";
    static final String SHELL = Convert.DEFAULT_PACKAGE + ".lastline";
    
    public static boolean isCoreJS(){
        String topjs = getTopJS();
        if ( topjs == null )
            return false;

        if ( topjs.equals( SHELL ) )
            return true;

        return topjs.startsWith( CORE );
    }
    
    public static String getTopJS(){
        
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        
        for ( int i=0; i<st.length; i++ ){
            StackTraceElement e = st[i];
            if ( e.getClassName().startsWith( Convert.DEFAULT_PACKAGE + "." ) )
                return e.getClassName();
        }

        return null;
    }
    
    
}
