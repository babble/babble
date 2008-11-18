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

/**
 *   Class to handle framework (aka 'app environment') management.
 */
public abstract class Framework {

    public abstract void install( AppContext context ) throws IOException;

    /**
     *   Load a known framework by name.  Currently, these are stored in the
     *   conf/frameworks.json file in the root of ed.  They should come from
     *   the cloud.
     *
     * @param name name of framework to configure
     * @param version of named framework to use
     * @return framewok object for the given name and version
     */
    public static Framework byName(String name, String version) {
        if (name == null) {
            throw new RuntimeException("Error : framework name can't be null");
        }

        return _frameworks.getFramework(name, version);
    }

    /**
     *   Load a framework by custom class.  The class must be in the appserver classpath,
     *   and must extend Framework
     *
     * @param classname name of class to use
     * @return instantiated framework class
     */
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

    /**
     *  Load a custom framework.  Caller provides a JSObject-digested JSON of the configuration.
     *
     * @param obj containing framework config JSON
     * @return framework object that in call of install() creates the framework specified
     */
    public static Framework byCustom(JSObjectBase obj) {

        if (obj == null) {
            throw new RuntimeException("Error : framework custom object can't be null");
        }

        return _frameworks.getCustomFramework(obj);
    }


    static {
        try {
            InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("frameworks.json");

            if (is == null) {
                throw new RuntimeException("Error : can't find frameworks.json file in the classpaths");
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
