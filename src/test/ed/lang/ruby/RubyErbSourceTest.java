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

package ed.lang.ruby;

import org.testng.annotations.*;
import static org.testng.Assert.*;

@Test(groups = {"ruby", "ruby.erbsource"})
public class RubyErbSourceTest extends ErbSourceRunner {

    @BeforeMethod(groups={"ruby", "ruby.erbsource"})
    public void setUp() {
        super.setUp();
        runJS("add_seven = function(i) { return i + 7; };" +
              "two_args = function(a, b) { return a + b; };" +
              "data = {};" +
              "data.count = 1;" +
              "data.subobj = {};" +
              "data.subobj.subvar = 99;" +
              "data.subobj.add_seven = add_seven;" +
              "data.add_seven = add_seven;" +
              "array = [100, \"test string\", null, add_seven];");
    }

    public void testSimple() {
        assertRubyEquals("<%= $data.count %>", "1");
    }

    public void testJSPrintWorksInline() {
        assertRubyEquals("foo <% $scope.print('bar') %> bletch", "foo bar bletch");
    }

    public void testJSPrintRestoredAfterRun() {
        runRuby("foo <% $scope.print('bar') %> bletch");
        assertJSEquals("print('hello, world!');", "hello, world!");
    }
}
