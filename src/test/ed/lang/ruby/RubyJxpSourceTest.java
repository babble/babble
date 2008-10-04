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

import java.io.File;

import org.jruby.runtime.builtin.IRubyObject;

import org.testng.annotations.*;
import static org.testng.Assert.*;

import ed.appserver.JSFileLibrary;
import ed.js.*;
import ed.js.engine.Scope;

@Test(groups = {"ruby", "ruby.jxpsource"})
public class RubyJxpSourceTest extends SourceRunner {

    @BeforeMethod(groups={"ruby", "ruby.jxpsource"})
    public void setUp() {
        super.setUp();
        runJS("add_seven = function(i) { return i + 7; };" +
              "two_args = function(a, b) { return a + b; };" +
              "data = {};" +
              "data.count = 1;" +
              "data.str = 'foo';" +
              "data.subobj = {};" +
              "data.subobj.subvar = 99;" +
              "data.subobj.add_seven = add_seven;" +
              "data.add_seven = add_seven;" +
              "array = [100, \"test string\", null, add_seven];");
    }

    public void testContent() {
        assertRubyEquals("puts 1 + 1", "2");
    }

    public void testGlobalsAreDefined() {
        runRuby("puts \"#{$data == nil ? 'oops: global $data is not defined' : 'ok'}\"");
        assertEquals(rubyOutput, "ok");
    }

    public void testNewGlobalsGetCreated() {
        runRuby("$foo = $data.count");
        assertEquals(s.get("foo").toString(), "1"); // proves $data exists and $foo is created
    }

    public void testScopeToVar() {
        assertRubyEquals("puts $data.count;", "1");
    }

    public void testCallFuncInVar() {
        assertRubyEquals("puts $data.add_seven(35)", "42");
    }

    public void testCallTopLevelFunc() {
        assertRubyEquals("puts add_seven(35)", "42");
    }

    public void testCallTopLevelFuncInsideFunc() {
        assertRubyEquals("def foo; puts add_seven(35); end; foo()", "42");
    }

    public void testCallTopLevelFuncInsideMethod() {
        assertRubyEquals("class Foo; def foo; puts add_seven(35); end; end; Foo.new.foo()", "42");
    }

    public void testGet() {
        assertRubyEquals("puts $data.get('count')", "1");
    }

    public void testGetUsingHashSyntax() {
        assertRubyEquals("puts $data['count']", "1");
    }

    public void testRubyModifiesJS() {
        runRuby("$data.count = 42");
        assertEquals(s.eval("data.count").toString(), "42");
    }

    public void testModifyUsingSet() {
        runRuby("$data.set('count', 42)");
        assertEquals(s.eval("data.count").toString(), "42");
    }

    public void testModifyUsingHash() {
        runRuby("$data['count'] = 42");
        assertEquals(s.eval("data.count").toString(), "42");
    }

    public void testCreateNew() {
        runRuby("$data['vint'] = 3; $data.vfloat = 4.2; $data.varray = [1, 2, 'three']; $data.vhash = {'a' => 1, 'b' => 2}");
        JSObject data = (JSObject)s.get("data");
        assertEquals(data.get("vint").toString(), "3");
        assertEquals(data.get("vfloat").toString(), "4.2");
        assertEquals(data.get("varray").toString(), "1,2,three");
        JSObject vhash = (JSObject)data.get("vhash");
        assertEquals(vhash.get("a").toString(), "1");
        assertEquals(vhash.get("b").toString(), "2");
    }

    public void testBuiltIn() {
        assertRubyEquals("puts $data.keySet.sort", "add_seven\ncount\nstr\nsubobj");
    }

    public void testSubObject() {
        assertRubyEquals("puts $data.subobj.subvar", "99");
        assertRubyEquals("puts $data.subobj.add_seven(35)", "42");
    }

    public void testReadScope() {
        assertRubyEquals("puts $scope.get('array')[1]", "test string");
    }

    public void testWriteScope() {
        runRuby("$scope.set('new_thing', 57)");
        assertNotNull(s.get("new_thing"), "'new_thing' not defined in scope");
        assertEquals(s.get("new_thing").toString(), "57");
    }

    public void testWriteScopeUsingHashSyntax() {
        runRuby("$scope['new_thing'] = 57");
        assertNotNull(s.get("new_thing"), "'new_thing' not defined in scope");
        assertEquals(s.get("new_thing").toString(), "57");
    }

    public void testArrayModsAreExported() {
        runRuby("$array[0] = 99");
        runJS("print(array[0]);");
        assertEquals(jsOutput, "99");
    }

    public void testInnerArrayModsAreExported() {
        runJS("data.array = [1, 2, 3];");
        runRuby("$data.array[0] = 99");
        runJS("print(data.array[0]);");
        assertEquals(jsOutput, "99");
    }

    public void testHashModsAreExported() {
        JSMap map = new JSMap();
        map.set(new JSString("a"), new Integer(1));
        map.set(new Long(42), new JSString("test string"));
        s.set("hash", map);

        // sanity test
        runRuby("puts \"#{$hash == nil ? 'oops: global $hash is not defined' : 'ok'}\"");
        assertEquals(rubyOutput, "ok");
        runRuby("s = $hash['a'].to_s; if s == '1'; puts 'ok'; else; puts \"oops: $hash['a'] is not defined properly; it is '#{s}'\"; end");
        assertEquals(rubyOutput, "ok");

        runRuby("$hash['a'] = 99");
        runJS("print(hash['a']);");
        assertEquals(jsOutput, "99");
    }

    public void testInnerHashModsAreExported() {
        JSMap map = new JSMap();
        map.set(new JSString("a"), new Integer(1));
        map.set(new Long(42), new JSString("test string"));
        ((JSObject)s.get("data")).set("hash", map);

        // sanity test
        runRuby("puts \"#{$data.hash == nil ? 'oops: global $data.hash is not defined' : 'ok'}\"");
        assertEquals(rubyOutput, "ok");
        runRuby("s = $data.hash['a'].to_s; if s == '1'; puts 'ok'; else; puts \"oops: $data.hash['a'] is not defined properly; it is '#{s}'\"; end");
        assertEquals(rubyOutput, "ok");

        Object o = ((JSObject)s.get("data")).get("hash");
        assertTrue(o instanceof JSMap, "oops: JS hash is " + o.getClass().getName() + ", not a JSMap");
        runRuby("$data.hash['a'] = 99");
        runJS("print(data.hash['a']);");
        assertEquals(jsOutput, "99");
    }

    public void testIncludeParentScopes() {
        s.put("adult", new Integer(555));
        Scope child = new Scope("child", s);
        child.put("kiddie", new Integer(666));
        s = child;
        assertRubyEquals("puts $adult; puts $kiddie", "555\n666");
    }

    // Make sure JS objects are getting methods defined in Ruby.
    public void testRubyExtensionsSeenByConvertedObjects() {
        assertRubyEquals("class Object\n" +
                         "  def to_param\n" +
                         "    to_s\n" +
                         "  end\n" +
                         "end\n" +
                         "begin\n" +
                         "  puts 'bar'.to_param\n" +
                         "  puts $data.str\n" +
                         "  puts $data.str.to_param\n" +
                         "rescue => ex\n" +
                         "  puts ex.to_s\n" +
                         "end",
                         "bar\nfoo\nfoo");
    }

//     public void testLibPathAdditions() {
//         s.put("local", new JSFileLibrary(new File("/foo/bar/local"), null, s));
//         s.put("core", new JSFileLibrary(new File("/foo/bar/core"), null, s));
//         s.put("external", new JSFileLibrary(new File("/foo/bar/external"), null, s));
//         runRuby("puts $:.join(',')");
//         assertTrue(rubyOutput.contains("/foo/bar/local"));
//         assertTrue(rubyOutput.contains("/foo/bar/core"));
//         assertTrue(rubyOutput.contains("/foo/bar/external"));
//     }

    public void testLibNameFromPath() {
        assertEquals(source.libNameFromPath("local"), "local");
        assertEquals(source.libNameFromPath("/local"), "local");
        assertEquals(source.libNameFromPath("local/foo"), "local");
        assertEquals(source.libNameFromPath("/local/foo"), "local");
    }

    public void testRemoveLibName() {
        assertEquals(source.removeLibName(""), "");
        assertEquals(source.removeLibName("local"), "");
        assertEquals(source.removeLibName("/local"), "");
        assertEquals(source.removeLibName("local/foo"), "foo");
        assertEquals(source.removeLibName("/local/foo"), "foo");
    }

    public void testGetLibFromPath() {
        Object local = s.get("local");
        assertSame(source.getLibFromPath("local", s), local);
        assertSame(source.getLibFromPath("local", s), local);
        assertSame(source.getLibFromPath("/local", s), local);
        assertSame(source.getLibFromPath("/local/foo", s), local);
        assertNull(source.getLibFromPath("no/such/lib/name", s));
    }
}
