// PyJSLogLevelWrapper.java

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

package ed.lang.python;

import java.util.*;

import org.python.core.*;
import org.python.expose.*;
import org.python.expose.generate.*;

import ed.js.*;
import ed.log.*;
import ed.js.engine.*;
import static ed.lang.python.Python.*;

@ExposedType(name = "log_level")
public class PyJSLogLevelWrapper extends PyString {

    public PyJSLogLevelWrapper( Level level ){
        super(level.toString());
        _level = level;
    }

    /*    @ExposedMethod
    public final boolean jswrapper___nonzero__(){
        return _js.keySet().size() > 0;
    }

    public boolean __nonzero__(){
        return jswrapper___nonzero__();
        }*/

    public String toString(){
        return "[wrapper for " + _level + "]";
    }

    @ExposedMethod(names = {"__repr__", "__str__"})
    public String log_level___repr__(){
        return _level.toString();
    }

    final Level _level;

}
