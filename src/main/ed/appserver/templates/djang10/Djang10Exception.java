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
import ed.js.JSObjectBase;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls2;

public class Djang10Exception extends RuntimeException implements JSObject {
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
    public java.util.Collection<String> keySet(){
        return inner.keySet();
    }
    public java.util.Collection<String> keySet( boolean includePrototype ){
        return inner.keySet(includePrototype);
    }
    public boolean containsKey( String s ){
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

    
    //Exception overrides
    public Throwable getCause() {
        return cause;
    }
    public String getMessage() {
        return detailMessage;
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
