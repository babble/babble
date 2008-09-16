// JS.java

/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package ed.js;

import java.io.*;

import ed.lang.*;
import ed.js.engine.*;

public class JS extends Language {

    public JS(){
        super( "js" );
    }

    public JSFunction compileLambda( String source ){     
        return Convert.makeAnon( source , true );
    }

    public static boolean JNI = false;
    public static final boolean DI = false;
    public static final boolean RAW_EXCPETIONS = ed.util.Config.get().getBoolean( "RAWE" );

    public static void _debugSI( String name , String place ){
        if ( ! DI )
            return;
        System.err.println( "Static Init : " + name + " \t " + place  );
    }

    public static void _debugSIStart( String name ){
        if ( ! DI )
            return;
        System.err.println( "Static Init : " + name + " Start" );
    }

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
