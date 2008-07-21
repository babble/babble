// JSONTest.java

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

import org.testng.annotations.Test;

import ed.js.*;
import ed.js.engine.*;

public class JSONTest extends ed.TestCase {

    @Test(groups = {"basic"})
    public void testJSONString(){
        String cases[] = {"abc", "\'", "\\", "\"", "\\4", "a\nb", "a\rb", "a\n", "a\\"};
        Scope scope = new Scope("test scope", null);

        for(int i = 0; i < cases.length; ++i){
            assertEquals(cases[i], scope.eval(JSON.serialize(cases[i])).toString());
            assertEquals(cases[i], scope.eval(JSON.serialize(new JSString(cases[i]))).toString());
        }

        Integer numbers[] = {41, -1, 292929};
        for(int i = 0; i < numbers.length; ++i){
            assertEquals(numbers[i], scope.eval(JSON.serialize(numbers[i])));
        }
        
        JSFunction blankf = (JSFunction)scope.eval("(function(){})");

        // JSON of a function can return the code to a function(){} expression
        // Wrap it in parens so that it's a lambda rather than a statement.
        assert(scope.eval("("+JSON.serialize(blankf)+")") instanceof JSFunction);

        // JSON of a Java function, though, should be a string like 
        // "JSFunction: blah"
        assert(scope.eval(JSON.serialize(new ed.js.engine.JSBuiltInFunctions.NewDate())) instanceof JSString);
        
        // JSON of a native Java object is a string too
        assert(scope.eval(JSON.serialize(new JSONTest())) instanceof JSString);

        ed.log.Logger log = ed.log.Logger.getLogger("test");

        // Logger is a special case, since it's a JS function
        assert(scope.eval(JSON.serialize(log)) instanceof JSString);

    }

    public static void main( String args[] ){
        (new JSONTest()).runConsole();
    }
}
