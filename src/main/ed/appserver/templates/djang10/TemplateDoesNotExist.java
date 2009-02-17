/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
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
