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

import java.util.*;

import org.jruby.Ruby;
import org.jruby.RubyClass;

import ed.js.engine.Scope;

/**
 * Scopes need to be wrapped differently because their key sets are a set
 * consiting of of the key set of itself and all its parent scopes.
 */
public class RubyScopeWrapper extends RubyJSObjectWrapper {

    RubyScopeWrapper(Scope s, Ruby runtime, Scope obj) {
	super(s, runtime, obj);
    }

    RubyScopeWrapper(Scope s, Ruby runtime, Scope obj, RubyClass klass) {
	super(s, runtime, obj, klass);
    }

    protected Collection<? extends Object> jsKeySet() {
	Set<String> keys = new HashSet<String>();
	Scope s = (Scope)_jsobj;
	while (s != null) {
	    keys.addAll(s.keySet());
	    s = s.getParent();
	}
	return keys;
    }
    
}
