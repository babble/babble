// Framework.java

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

import ed.appserver.*;
import ed.js.JSObjectBase;

import java.io.IOException;
import java.io.InputStream;

public abstract class Framework {

    public abstract void install( AppContext context ) throws IOException;

    public static Framework forName( String name ){
        if (name.equalsIgnoreCase("AppEngine")) {
            return new AppEngine();
        }
        return null;
    }

    public static Framework byName(String name, String version) {
        if (name == null) {
            throw new RuntimeException("Error : framework name can't be null");
        }

        return _frameworks.getFramework(name, version);
    }

    public static Framework byClass(String classname) {
        try {
            Object o =  Class.forName(classname).newInstance();

            if (o instanceof Framework) {
                return (Framework) o;
            }

            throw new RuntimeException("Error : problem instantiating framework by class name - wasn't a Framework [" +
                classname + "]");
        } catch (Exception e) {
            throw new RuntimeException("Error : problem instantiating framework by class name [" +
                    classname + "]", e);
        }
    }

    public static Framework byCustom(JSObjectBase obj) {

        if (obj == null) {
            throw new RuntimeException("Error : framework custom object can't be null");
        }

        return _frameworks.getCustomFramework(obj);
    }


    static {
        try {
            InputStream is = Framework.class.getResourceAsStream("/conf/frameworks.json");

            if (is == null) {
                System.err.println("No frameworks.json file found");
            }
            else {
                _frameworks = new PredefinedFrameworks(is);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static PredefinedFrameworks _frameworks;
}
