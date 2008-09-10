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

public class Djang10Exception extends RuntimeException implements JSObject {
    private String detailMessage;
    private Throwable cause;
    
    public Djang10Exception() { }

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
    
    //Minimal JSObject
    public JSFunction getConstructor() {
        return cons;
    }
    public JSObject getSuper() {
        return null;
    }
    public Object get( Object n ){
        return null;
    }
    public java.util.Collection<String> keySet(){
        throw new UnsupportedOperationException();
    }
    public java.util.Collection<String> keySet( boolean includePrototype ){
        throw new UnsupportedOperationException();
    }
    public boolean containsKey( String s ){
        throw new UnsupportedOperationException();
    }
    public Object set( Object n , Object v ){
        throw new UnsupportedOperationException();
    }
    public Object setInt( int n , Object v ){
        throw new UnsupportedOperationException();
    }
    public Object getInt( int n ){
        throw new UnsupportedOperationException();
    }
    public Object removeField( Object n ){
        throw new UnsupportedOperationException();
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
