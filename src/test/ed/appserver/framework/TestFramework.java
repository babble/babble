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

package ed.appserver.framework;

import java.io.IOException;

import ed.appserver.AppContext;
import ed.appserver.frameworks.Framework;
import ed.appserver.adapter.AdapterType;
import ed.js.JSObject;
import ed.js.JSDict;

/**
 *   Simple test class to test framework via class
 */
public class TestFramework extends Framework {
    public void install (AppContext context) throws IOException {

        context.setStaticAdapterTypeValue(AdapterType.CGI);

        JSObject packages = (JSObject)context.getConfigObject("packages");

        if (packages == null) {
            packages = new JSDict();
        }

        JSObject google = new JSDict();
        google.set("module", "py-google");
        google.set("path", "");
        packages.set("floogie", google);

        context.setConfigObject("packages", packages);

        context.runInitFile("_init_for_test.js");
    }
}
