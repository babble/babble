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

import org.jruby.Ruby;
import org.jruby.RubyClass;

import ed.appserver.JSFileLibrary;
import ed.js.JSObject;
import ed.js.engine.Scope;

/**
 * The JSFileLibrary wrapper implements peek() in a way that makes sure the
 * file library object is not initialized.
 */
public class RubyJSFileLibraryWrapper extends RubyJSFunctionWrapper {

    RubyJSFileLibraryWrapper(Scope s, Ruby runtime, JSFileLibrary obj, String name, RubyClass eigenclass) {
	this(s, runtime, obj, name, eigenclass, null);
    }

    RubyJSFileLibraryWrapper(Scope s, Ruby runtime, JSFileLibrary obj, String name, RubyClass eigenclass, JSObject jsThis) {
	super(s, runtime, obj, name, eigenclass, jsThis);
    }

    public Object peek(Object key) { return ((JSFileLibrary)_jsobj).get(key, false); }
}
