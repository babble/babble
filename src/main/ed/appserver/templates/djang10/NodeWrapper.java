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

import ed.appserver.templates.djang10.Parser.Token;
import ed.js.JSException;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;
import ed.js.func.JSFunctionCalls2;
import ed.log.Level;
import ed.log.Logger;

//hacks to allow backwards compatibility with print & DEBUGING of render calls
public class NodeWrapper {
    
    public static JSObject wrap(JSObject node, Token token) {
        node.set("render", new RenderWrapperFunc((JSFunction)node.get("render")));
        node.set("__render", new __RenderWrapperFunc((JSFunction)node.get("__render")));
        node.set("__token", token);
        
        return node;
    }
    
    private static final class RenderWrapperFunc extends JSFunctionCalls1 {
        private final Logger log = Logger.getRoot().getChild("djang10").getChild("NodeWrapper");
        private final JSFunction renderFunc;
        
        public RenderWrapperFunc(JSFunction renderFunc) {
            this.renderFunc = renderFunc;
        }

        public Object call(Scope scope, Object contextObj, Object[] extra) {
            JSObject thisObj = (JSObject)scope.getThis();
            Context context = (Context)contextObj;
            
            if(log.getEffectiveLevel().compareTo(Level.DEBUG) <= 0) {
                String selfRepr = "Unkown";
                String location = "Unknown:??";
                try { selfRepr = thisObj.toString(); } catch(Exception ignored) {}
                try { 
                    Token token = (Token)thisObj.get("__token");
                    location = token.getOrigin() + ":" +token.getStartLine();
                }catch(Exception e) {}
                log.debug("Rendering: " + selfRepr + "(" + location + ")");
            }
            
            scope = scope.child();
            PrintWrapperFunc printWrapper = new PrintWrapperFunc();
            scope.set("print", printWrapper);
            
            Object ret;
            
            context.__begin_render_node(thisObj);
            try {
                ret = renderFunc.callAndSetThis(scope, thisObj, new Object[] { contextObj });
            } 
            catch(TemplateRenderException e) {
                throw e;
            }
            catch(Exception e) {
                throw new TemplateRenderException(context, e);
            }
            finally {
                context.__end_render_node(thisObj);
            }
            
            if(printWrapper.buffer.length() > 0)
                return printWrapper.buffer + (ret == null? "" : ret.toString());
            else
                return ret;
        }
    };
    private static final class __RenderWrapperFunc extends JSFunctionCalls2 {
        private final Logger log = Logger.getRoot().getChild("djang10").getChild("NodeWrapper");
        private final JSFunction __renderFunc;
        
        public __RenderWrapperFunc(JSFunction func) {
            __renderFunc = func;
        }
        
        public Object call(Scope scope, Object contextObj, Object printer, Object[] extra) {
            JSObject thisObj = (JSObject)scope.getThis();
            Context context = (Context)contextObj;

            if(log.getEffectiveLevel().compareTo(Level.DEBUG) <= 0) {
                String selfRepr = "Unkown";
                String location = "Unknown:??";
                try { selfRepr = thisObj.toString(); } catch(Exception t) {}
                try { 
                    Token token = (Token)thisObj.get("__token");
                    location = token.getOrigin() + ":" +token.getStartLine();
                }catch(Exception e) {}
                
                log.debug("__Rendering: " + selfRepr + "(" + location + ")");
            }
            
            scope = scope.child();
            scope.setGlobal(true);
            scope.put("print", printer, true);
            
            context.__begin_render_node(thisObj);
            try {
                __renderFunc.callAndSetThis(scope, thisObj, new Object[] { contextObj, printer });
            }
            catch(TemplateRenderException e) {
                throw e;
            }
            catch(JSException e) {
                if(e.getCause() instanceof TemplateRenderException)
                    throw e;
                
                Exception t = (e.getCause() instanceof Exception)? (Exception)e.getCause() : e;
                throw new TemplateRenderException(context, t);
            }
            catch(Exception e) {
                throw new TemplateRenderException(context, e);
            }
            finally {
                context.__end_render_node(thisObj);
            }
            
            return null;
        }

        
    };
    
    public static class PrintWrapperFunc extends JSFunctionCalls1 {
        public final StringBuilder buffer = new StringBuilder();
        private final Logger logger = Logger.getRoot().getChild("djang10").getChild("NodeWrapper");
        
        public Object call(Scope scope, Object p0, Object[] extra) {
            JSObject thisObj = (JSObject)scope.getThis();
            String location = "Unknown:??";
            try { 
                Token token = (Token)thisObj.get("__token");
                location = token.getOrigin() + ":" +token.getStartLine();
            }catch(Exception e) {}
            
            logger.error("calling print while rendering has undefined behavior which will change in the future. (" + location + ")");
            
            buffer.append(p0);
            return null;
        }
    }
}
