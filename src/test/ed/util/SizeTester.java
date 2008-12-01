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

package ed.util;

import org.testng.annotations.Test;

import ed.js.JSFunction;
import ed.js.func.JSFunctionCalls1;
import ed.js.engine.Convert;
import ed.js.engine.Scope;
import ed.util.ScriptTestInstance;

import java.io.File;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import sizeof.agent.SizeOfAgent;

/**
 * Dynamic test instance for testing any 10genPlatform script
 * 
 * Code stolen lock, stock and barrel from ConvertTest.  Uses exact same convention
 * and scheme for comparing output
 */
public class SizeTester extends ScriptTestInstanceBase {

    SizeTester() {
        super();
    }

    public JSFunction convert() throws Exception {
        File f = getTestScriptFile();
        Convert c = new Convert( f );
        return c.get();
    }

    public void preTest(Scope scope) throws Exception {
        final PrintStream out = new PrintStream( new ByteArrayOutputStream() );
        JSFunction myout = new JSFunctionCalls1(){
                public Object call( Scope scope ,Object o , Object extra[] ){
                    return null;
                }
            };
        scope.put( "print" , myout , true );
    }

    public void validateOutput(Scope scope) throws Exception {
        long instApprox = SizeOfAgent.fullSizeOf( scope );
        long edApprox = scope.approxSize();
        if( edApprox * 1.1 < instApprox || edApprox * .9 > instApprox )
            throw new RuntimeException( "too big a size diff in scope:\n"+
                                        "\taccording to ed: "+edApprox+"\n"+
                                        "\taccording to java.lang.Instrument.sizeOfObject: "+instApprox);
        else 
            System.out.print( "." );
    }


    public static void main(String args[]) 
        throws Exception {
        if( args.length == 0 ) {
            System.out.println("No tests selected.  Exiting.");
            return;
        }

        for( String a : args ) {
            SizeTester st = new SizeTester();
            st.setTestScriptFile( new File( "./src/test/ed" + File.separator + a ) );
            st.test();
        }
        System.out.println();
    }
}
