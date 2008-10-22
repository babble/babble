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

import ed.lang.ruby.Loader;

@Test(groups = {"ruby", "ruby.loader"})
public class LoaderTest extends SourceRunner {

    private Loader loader;

    @BeforeMethod(groups={"ruby", "ruby.loader"})
    public void setUp() {
        super.setUp();
        loader = new Loader(s);
    }

    public void testLibNameFromPath() {
        assertEquals(loader.libNameFromPath("local"), "local");
        assertEquals(loader.libNameFromPath("/local"), "local");
        assertEquals(loader.libNameFromPath("local/foo"), "local");
        assertEquals(loader.libNameFromPath("/local/foo"), "local");
    }

    public void testRemoveLibName() {
        assertEquals(loader.removeLibName(""), "");
        assertEquals(loader.removeLibName("local"), "");
        assertEquals(loader.removeLibName("/local"), "");
        assertEquals(loader.removeLibName("local/foo"), "foo");
        assertEquals(loader.removeLibName("/local/foo"), "foo");
    }

    public void testGetLibFromPath() {
        Object local = s.get("local");
        assertSame(loader.getLibFromPath("local"), local);
        assertSame(loader.getLibFromPath("/local"), local);
        assertSame(loader.getLibFromPath("local/foo"), local);
        assertSame(loader.getLibFromPath("/local/foo"), local);
        assertNull(loader.getLibFromPath("no/such/lib/name"));
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
}
