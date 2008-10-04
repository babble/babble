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

import org.jruby.Ruby;
import org.jruby.RubyClass;

import ed.lang.ruby.RubyDBCursorWrapper;
import ed.db.DBCursor;
import ed.js.engine.Scope;

@Test(groups = {"ruby"})
public class RubyDBCursorWrapperTest {

    Scope s;
    org.jruby.Ruby r;

    @BeforeMethod
    public void setUp() {
        s = new Scope("test", null);
        r = org.jruby.Ruby.newInstance();
    }

    public void testClass() {
        RubyClass klazz = new RubyDBCursorWrapper(s, r, new DBCursor(null, null, null, null)).type();
        assertEquals(klazz.name().toString(), "DBCursor");
        assertEquals(((RubyClass)klazz.superclass(r.getCurrentContext())).name().toString(), "Array");
    }
}
