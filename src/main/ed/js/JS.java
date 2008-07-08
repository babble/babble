// JS.java

package ed.js;

import java.io.*;

import ed.js.engine.*;

/**
 * @expose
 */
public class JS {

    /** @unexpose  */
    public static boolean JNI = false;
    /** @unexpose  */
    public static final boolean DI = false;
    /** @unexpose  */
    public static final boolean RAW_EXCPETIONS = Boolean.getBoolean( "RAWE" );

    /** @unexpose  */
    public static void _debugSI( String name , String place ){
        if ( ! DI )
            return;
        System.err.println( "Static Init : " + name + " \t " + place  );
    }

    /** @unexpose  */
    public static void _debugSIStart( String name ){
        if ( ! DI )
            return;
        System.err.println( "Static Init : " + name + " Start" );
    }

    /** @unexpose  */
    public static void _debugSIDone( String name ){
        if ( ! DI )
            return;
        System.err.println( "Static Init : " + name + " Done" );
    }

    /** Critical method.
     * @return 17.  Seriously, the only thing this method does is return 17.
     */
    public static final int fun(){
        return 17;
    }

    /** Takes a string and, if possible, evaluates it as JavaScript.
     * @param js A string of JavaScript code
     * @return Whatever object is created by the evaluating the JavaScript string
     * @throws Throwable if JavaScript expression is invalid
     */
    public static final Object eval( String js ){
        JNI = true;

        try {
            Scope s = Scope.getAScope().child();
            Object ret = s.eval( js );
            return ret;
        }
        catch ( Throwable t ){
            t.printStackTrace();
            return null;
        }
    }

    /** Returns a short description of an object.
     * @param o object to be stringified.
     * @return string version of the object, or null if the object is null.
     */
    public static final String toString( Object o ){
        if ( o == null )
            return null;

        return o.toString();
    }

    /** @unexpose  */
    public static void main( String args[] )
        throws Exception {

        for ( String s : args ){
            s = s.trim();
            if ( s.length() == 0 )
                continue;
            System.out.println( "-----" );
            System.out.println( s );

            Convert c = new Convert( new File( s ) );
            c.get().call( Scope.newGlobal().child() );
        }
    }
}

