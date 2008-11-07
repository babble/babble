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

package ed.appserver.frameworks;

import ed.appserver.AppContext;
import ed.appserver.frameworks.Framework;
import ed.js.JSObject;
import ed.js.JSDict;
import ed.js.JSString;

public class AppEngine extends Framework {
    public void install (AppContext context) {
        // Set up mapping "import google" => "import core.modules.py-google".
        JSObject packages = (JSObject)context.getConfigObject("packages");
        if (packages == null) {
            packages = new JSDict();
        }
        packages.set("google", "py-google");
        context.setConfigObject("packages", packages);

        // Set up adaptor type.
        context.setInitObject("adapterType", new JSString("CGI"));
    };
}
