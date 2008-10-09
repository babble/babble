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
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

import ed.MyAsserts;
import ed.io.StreamUtil;
import ed.js.JSFunction;
import ed.js.func.JSFunctionCalls1;
import ed.js.engine.*;
import ed.util.ScriptTestInstanceBase;
import ed.util.ScriptTestFactory;
import ed.appserver.JSFileLibrary;
import ed.lang.python.PythonJxpSource;

public class MozillaTestInstance extends ScriptTestInstanceBase{

    public MozillaTestInstance(){
    }

    public Scope scope;

    ByteArrayOutputStream bout = new ByteArrayOutputStream();

    private void runShell( Scope scope, File shellFile ) {
        try {
            Convert c = new Convert( shellFile );
            JSFunction f = c.get();
            ((JSCompiledScript)f).setPath( new JSFileLibrary( shellFile.getParentFile() , "local" , scope ) );
            f.call( scope );
        }
        catch ( Exception e ){
            e.printStackTrace();
        }
    }

    public JSFunction convert() throws Exception{
        File f = getTestScriptFile();

        // run all the shell.js scripts before the test
        // +1 for the final "/"
        String subpath = f.getCanonicalPath().substring( ScriptTestFactory.baseDir.length() + 1 );
        String path[] = subpath.split( File.separator );
        String tempBase = ScriptTestFactory.baseDir + File.separator;
        
        runShell( scope, new File( tempBase + "shell.js" ));
        for( int i=0; i<path.length-1; i++ ) {
            tempBase = tempBase + path[i] + File.separator;
            runShell( scope, new File( tempBase + "shell.js" ) );
        }

        Convert c = new Convert( f );
        return c.get();
    }

    public void preTest(Scope scope) throws Exception {
        this.scope = scope;
        // set it up to only print errors
        final PrintStream out = new PrintStream( bout );
        
        JSFunction myout = new JSFunctionCalls1(){
                public Object call( Scope scope ,Object o , Object extra[] ){
                    String str = ed.js.JSInternalFunctions.JS_toString( o );
                    if( str.startsWith( " FAILED!" ) )
                        out.println( str );
                    return null;
                }
            };
        scope.put( "print" , myout , true );    	
    }

    public void validateOutput(Scope scope) throws Exception {
        scope = null;
        String output = bout.toString().trim();
        try {
            assertEmptyString( output );
        }
        catch( MyAssert a ) {
            throw a;
        }
    }

    public static void main(String[] args) throws Exception {
        MozillaTestInstance ct = new MozillaTestInstance();
        if( args.length > 0 ) {
            System.out.println( "only testing "+args[0] );
            ct.setTestScriptFile( new File( ScriptTestFactory.baseDir + File.separator + args[0] ) );
            ct.test();
        }
        else {
            System.out.println("no tests selected");
        }
    }
}
