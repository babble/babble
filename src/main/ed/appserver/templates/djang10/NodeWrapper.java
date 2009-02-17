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

import ed.appserver.templates.djang10.Parser.Token;
import ed.js.JSException;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSObjectSize;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;
import ed.js.func.JSFunctionCalls2;
import ed.log.Level;
import ed.log.Logger;
import ed.util.SeenPath;

//hacks to allow backwards compatibility with print & DEBUGING of render calls
public class NodeWrapper {

    public static JSObject wrap(JSObject node, Token token) {
        if(!(node.get( "render" ) instanceof JSFunction))
            throw new IllegalArgumentException("The node must contain a render method");
        if(!(node.get( "__render" ) instanceof JSFunction))
            throw new IllegalArgumentException("The node must contain a __render method");

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

            Object oldGlobalPrinter = scope.get( "print" );
            PrintWrapperFunc printWrapper = new PrintWrapperFunc();
            scope.put("print", printWrapper, false);


            Object ret;

            context.__begin_render_node(thisObj);
            try {
                ret = renderFunc.callAndSetThis(scope, thisObj, new Object[] { contextObj });
            }
            catch(TemplateRenderException e) {
                throw e;
            }
            catch(JSException e) {
                RuntimeException re = JSHelper.unnestJSException(e);

                if(re instanceof TemplateRenderException)
                    throw e;

                throw new TemplateRenderException(context, re);
            }
            catch(Exception e) {
                throw new TemplateRenderException(context, e);
            }
            finally {
                context.__end_render_node(thisObj);
                scope.put( "print" , oldGlobalPrinter, false );
            }

            if(printWrapper.buffer.length() > 0)
                return printWrapper.buffer + (ret == null? "" : ret.toString());
            else
                return ret;
        }

        public long approxSize(SeenPath seen) {
            long sum = super.approxSize( seen );

            sum += JSObjectSize.size( this.renderFunc, seen, this );
            sum += JSObjectSize.size( this.log, seen, this );

            return sum;
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

            Object oldGlobalPrinter = scope.get( "print" );
            scope.put("print", printer, false);

            context.__begin_render_node(thisObj);
            try {
                __renderFunc.callAndSetThis(scope, thisObj, new Object[] { contextObj, printer });
            }
            catch(TemplateRenderException e) {
                throw e;
            }
            catch(JSException e) {
                RuntimeException re = JSHelper.unnestJSException(e);

                if(re instanceof TemplateRenderException)
                    throw e;

                throw new TemplateRenderException(context, re);
            }
            catch(Exception e) {
                throw new TemplateRenderException(context, e);
            }
            finally {
                context.__end_render_node(thisObj);
                scope.put( "print" , oldGlobalPrinter, false );
            }

            return null;
        }
        public long approxSize(SeenPath seen) {
            long sum = super.approxSize( seen );

            sum += JSObjectSize.size( this.__renderFunc, seen, this );
            sum += JSObjectSize.size( this.log, seen, this );

            return sum;
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
        public long approxSize(SeenPath seen) {
            long sum = super.approxSize( seen );

            sum += JSObjectSize.size( this.logger, seen, this );
            if( seen.shouldVisit( buffer , this ) )
                sum += JSObjectSize.OBJ_OVERHEAD + ( 2 * buffer.capacity() );

            return sum;
        }
    }
}
