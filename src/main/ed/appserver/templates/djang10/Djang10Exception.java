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
import ed.js.JSObjectBase;
import ed.js.JSObjectSize;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls2;
import ed.util.SeenPath;
import ed.util.Sizable;

public class Djang10Exception extends RuntimeException implements JSObject, Sizable {
    private String detailMessage;
    private Throwable cause;

    protected JSObjectBase inner;

    public Djang10Exception() {
        inner = new JSObjectBase(getConstructor());
    }

    public Djang10Exception(String detailMessage) {
        this();
        this.detailMessage = detailMessage;
    }

    public Djang10Exception(String detailMessage, Throwable t) {
        this(detailMessage);
        this.cause = t;
    }

    protected void init(String detailMessage, Throwable cause) {
        this.detailMessage = detailMessage;
        this.cause = cause;
    }

    //Delegate to JSObject
    public JSFunction getConstructor() {
        return cons;
    }
    public JSObject getSuper() {
        return inner.getSuper();
    }
    public Object get( Object n ){
        return inner.get(n);
    }
    public java.util.Set<String> keySet(){
        return inner.keySet();
    }
    public java.util.Set<String> keySet( boolean includePrototype ){
        return inner.keySet(includePrototype);
    }
    public boolean containsKey( String s ){
        return containsKey( s , true );
    }
    public boolean containsKey( String s , boolean includePrototype ){
        return inner.containsKey(s);
    }
    public Object set( Object n , Object v ){
        return inner.set(n, v);
    }
    public Object setInt( int n , Object v ){
        return inner.setInt(n, v);
    }
    public Object getInt( int n ){
        return inner.getInt(n);
    }
    public Object removeField( Object n ){
        return inner.removeField(n);
    }
    public JSFunction getFunction( String name ){
        return JSObjectBase.getFunction( this , name );
    }

    //Exception overrides
    public Throwable getCause() {
        return cause;
    }
    public String getMessage() {
        return detailMessage;
    }

    public long approxSize(SeenPath seen) {
        long sum = JSObjectSize.OBJ_OVERHEAD;

        sum += JSObjectSize.size( detailMessage , seen , this );
        sum += JSObjectSize.size( cause , seen , this );
        sum += JSObjectSize.size( inner , seen , this );

        return sum;
    }

    public static final Constructor cons = new Constructor();

    private static class Constructor extends JSFunctionCalls2 {
        public JSObject newOne() {
            return new Djang10Exception();
        }
        public Object call(Scope scope, Object detailMessageObj, Object causeObj, Object[] extra) {
            Djang10Exception thisObj = (Djang10Exception)scope.getThis();

            thisObj.init((detailMessageObj == null)? null : detailMessageObj.toString(), (Throwable)causeObj);

            return null;
        }
    }
}
