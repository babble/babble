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

package ed.appserver.templates.djang10;

import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls2;

public class TemplateDoesNotExist extends Djang10Exception {
    private String path;

    private TemplateDoesNotExist() {}
    public TemplateDoesNotExist(String path) {
        init(path, null);
    }

    protected void init(String path, Throwable cause) {
        super.init("Template doesn't exist: " + path, cause);
        this.path = path;
    }
    public JSFunction getConstructor() {
        return cons;
    }
    
    public String getPath() {
        return path;
    }
    
    public static final Constructor cons = new Constructor();

    private static class Constructor extends JSFunctionCalls2 {
        public JSObject newOne() {
            return new TemplateDoesNotExist();
        }
        public Object call(Scope scope, Object pathObj, Object causeObj, Object[] extra) {
            TemplateDoesNotExist thisObj = (TemplateDoesNotExist)scope.getThis();
            thisObj.init(pathObj.toString(), (Exception)causeObj);
            return null;
        }
    }
}
