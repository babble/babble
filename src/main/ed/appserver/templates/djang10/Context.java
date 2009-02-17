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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.Set;

import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSObjectBase;
import ed.js.JSObjectSize;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls0;
import ed.js.func.JSFunctionCalls1;
import ed.util.OrderedSet;
import ed.util.SeenPath;

public class Context extends JSObjectBase {
    public static final String PUSH = "push";
    public static final String POP = "pop";

    private Stack<JSObject> renderStack;

    public final static JSFunction CONSTRUCTOR = new JSFunctionCalls1() {
        public JSObject newOne() {
            return new Context();
        }

        public Object call(Scope scope, Object p0, Object[] extra) {
            if (p0 != null)
                ((Context) scope.getThis()).rebase((JSObject) p0);

            return null;
        }

        protected void init() {
            _prototype.set(PUSH, new JSFunctionCalls0() {
                @Override
                public Object call(Scope scope, Object[] extra) {
                    ((Context) scope.getThis()).push();
                    return null;
                }
            });
            _prototype.set(POP, new JSFunctionCalls0() {
                @Override
                public Object call(Scope scope, Object[] extra) {
                    ((Context) scope.getThis()).pop();
                    return null;
                }
            });
        }
    };

    private static final String[] SPECIAL_PROPS = new String[] { "prototype", "__proto__", "constructor", "__constructor__",
            "__parent__" };
    private static final List<String> SPECIAL_PROPS_LIST;
    static {
        Arrays.sort(SPECIAL_PROPS);
        SPECIAL_PROPS_LIST = Arrays.asList(SPECIAL_PROPS);
    }

    private final LinkedList<JSObject> objectStack;

    private Context() {
        objectStack = new LinkedList<JSObject>();
        objectStack.add(new JSObjectBase());

        renderStack = new Stack<JSObject>();

        setConstructor(CONSTRUCTOR);
    }

    public Context(JSObject obj) {
        this();
        rebase(obj);
    }

    private void rebase(JSObject obj) {
        this.objectStack.clear();
        this.objectStack.addFirst(obj);

    }

    public boolean containsKey(String s) {
        if (SPECIAL_PROPS_LIST.contains(s))
            return super.containsKey(s);

        for (JSObject obj : objectStack) {
            if (obj.keySet().contains(s.toString()))
                return true;
        }
        return super.containsKey(s);
    }

    public Object _simpleGet(String s) {
        if (SPECIAL_PROPS_LIST.contains(s))
            return super._simpleGet(s);

        for (JSObject obj : objectStack) {
            if ((obj instanceof Scope) || obj.containsKey(s))
                return obj.get(s);
        }
        return super._simpleGet(s);
    }

    public Set<String> keySet( boolean includePrototype ) {
        Set<String> compositeKeySet = new OrderedSet<String>();

        for (JSObject obj : objectStack) {
            compositeKeySet.addAll(obj.keySet());
        }
        compositeKeySet.addAll(super.keySet( includePrototype ));

        return compositeKeySet;
    }

    public Object removeField(Object n) {
        if (SPECIAL_PROPS_LIST.contains(n))
            return super.removeField(n);

        for (JSObject obj : objectStack) {
            if (obj.containsKey(n.toString())) {
                return obj.removeField(n);
            }
        }
        return super.removeField(n);
    }

    public Object set(Object n, Object v) {
        if (SPECIAL_PROPS_LIST.contains(n))
            return super.set(n, v);

        return objectStack.getFirst().set(n, v);
    }

    public void push() {
        objectStack.addFirst(new JSObjectBase());
    }

    public void pop() {
        if (objectStack.size() <= 1)
            throw new IllegalStateException("Can't remove the last backing object");
        objectStack.remove();
    }

    public void __set_root(String key, Object value) {
        objectStack.getLast().set(key, value);
    }

    public void __begin_render_node(JSObject node) {
        renderStack.push(node);
    }
    public void __end_render_node(JSObject node) {
        renderStack.pop();
    }
    public Stack<JSObject> getRenderStack() {
        return renderStack;
    }

    public long approxSize(SeenPath seen) {
        long sum = super.approxSize( seen );
        sum += JSObjectSize.size( this.objectStack, seen, this );
        sum += JSObjectSize.size( this.renderStack, seen, this );
        return sum;
    }
}
