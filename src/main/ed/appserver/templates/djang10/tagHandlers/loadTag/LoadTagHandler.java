package ed.appserver.templates.djang10.tagHandlers.loadTag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ed.appserver.jxp.JxpSource;
import ed.appserver.templates.djang10.JSHelper;
import ed.appserver.templates.djang10.Library;
import ed.appserver.templates.djang10.Node;
import ed.appserver.templates.djang10.Parser;
import ed.appserver.templates.djang10.TemplateException;
import ed.appserver.templates.djang10.Node.TagNode;
import ed.appserver.templates.djang10.Parser.Token;
import ed.appserver.templates.djang10.filters.Filter;
import ed.appserver.templates.djang10.generator.JSWriter;
import ed.appserver.templates.djang10.tagHandlers.TagHandler;
import ed.js.JSFunction;
import ed.js.engine.Scope;

public class LoadTagHandler implements TagHandler {
    private static final String MODULE_LIST = "moduleList";

    public TagNode compile(Parser parser, String command, Token token) throws TemplateException {

        Scope callingScope = Scope.getThreadLocal();
        JSHelper helper = (JSHelper) callingScope.get(JSHelper.NS);
        
        LoadTagStateVar stateVar = parser.getStateVariable(getClass());
        if (stateVar == null) {
            stateVar = new LoadTagStateVar();
            parser.setStateVariable(getClass(), stateVar);
        }
        boolean isFirst = stateVar.libraryCount == 0;
        
        String moduleName = token.getContents().trim().split("\\s+", 2)[1];
        if (!moduleName.matches("^\\w+$"))
            throw new TemplateException("invalid module name: " + moduleName);

        JSFunction module = helper.loadModule(moduleName);
        if (module == null)
            throw new TemplateException("failed to find module: " + moduleName);
        parser.getTracker().addDependency((JxpSource) module.get(JxpSource.JXP_SOURCE_PROP));

        Library moduleLibrary = helper.callModule(callingScope, moduleName);
        

        // pull out the tags
        for (String tagName : moduleLibrary.getTags().keySet()) {
            stateVar.tagLibraryIndex.put(tagName, stateVar.libraryCount);
            parser.getTagHandlers().put(tagName, new ModuleTagHandlerWrapper(stateVar.libraryCount, (JSFunction)moduleLibrary.getTags().get(tagName)));
        }


        // pull out filters
        ModuleFilterWrapper filterWrapper = new ModuleFilterWrapper(stateVar.libraryCount);
        for (String filterName : moduleLibrary.getFilters().keySet()) {
            stateVar.filterLibraryIndex.put(filterName, stateVar.libraryCount);
            parser.getFilters().put(filterName, filterWrapper);
        }

        stateVar.libraryCount++;

        return new LoadNode(token, moduleName, isFirst);
    }

    public Map<String, JSFunction> getHelpers() {
        Map<String, JSFunction> helpers = new HashMap<String, JSFunction>();
        
        helpers.put(JSRenderPhaseParser.NAME, JSRenderPhaseParser.CONSTRUCTOR);
        helpers.put(JSRenderPhaseNode.NAME, JSRenderPhaseNode.CONSTRUCTOR);
        
        return helpers;
    }

    private static class LoadTagStateVar {
        public final Map<String, Integer> tagLibraryIndex = new HashMap<String, Integer>();
        public final Map<String, Integer> filterLibraryIndex = new HashMap<String, Integer>();
        public int libraryCount = 0;
    }

    private static final class LoadNode extends TagNode {
        private final String moduleName;
        private final boolean isFirst;

        public LoadNode(Token token, String moduleName, boolean isFirst) {
            super(token);
            this.moduleName = moduleName;
            this.isFirst = isFirst;
        }

        public void toJavascript(JSWriter preamble, JSWriter buffer) throws TemplateException {

            // TODO: move the isFirst check here....if the render function isn't
            // called for the first node, all's broken

            if (isFirst)
                preamble.append("var " + MODULE_LIST + " = [];\n");

            preamble.append(startLine, MODULE_LIST + ".push(" + JSHelper.NS + "." + JSHelper.CALL_MODULE + "(\"" + moduleName
                    + "\"));\n");
        }
    }

    private static class ModuleFilterWrapper implements Filter {
        private final int moduleIndex;

        public ModuleFilterWrapper(int moduleIndex) {
            this.moduleIndex = moduleIndex;
        }

        public String toJavascript(String filterName, String compiledValue, String compiledParam) throws TemplateException {
            return MODULE_LIST + "[" + moduleIndex + "].filters." + filterName + "(" + compiledValue + ", " + compiledParam + ")";
        }
    }
    
    private static class ModuleTagHandlerWrapper implements TagHandler {
        private JSFunction compilationFunc;
        private int moduleIndex;
        
        public ModuleTagHandlerWrapper(int moduleIndex, JSFunction compilationFunc) {
            super();
            this.moduleIndex = moduleIndex;
            this.compilationFunc = compilationFunc;
        }
        public TagNode compile(Parser parser, final String command, final Token token) throws TemplateException {
            JSCompilationPhaseParser compilationParser = new JSCompilationPhaseParser(parser);
            Scope compileScope = Scope.getThreadLocal().child();
            compileScope.setGlobal(true);
            compilationFunc.call(compileScope, compilationParser, token);
            
            final List<Node> childNodes = compilationParser.getAndClearNodes();
            
            return new TagNode(token) {
                public void toJavascript(JSWriter preamble, JSWriter buffer) throws TemplateException {
                    buffer.append("print(");
                    buffer.append(MODULE_LIST + "[" + moduleIndex + "].tags." + command + "(");
                    //serialize the parser
                    buffer.append(" new " + JSHelper.NS + "." + JSRenderPhaseParser.NAME + "([");
                    //serialize the immediate children using the tag contents and the javascript they generated
                    for(int i=0; i<childNodes.size(); i++) {
                        String tag = (childNodes.get(i) instanceof TagNode)? ((TagNode)childNodes.get(i)).tagName : "";
                        
                        if(i != 0) buffer.append(", ");
                        
                        buffer.append("new " + JSHelper.NS + "." + JSRenderPhaseNode.NAME + "(\"" + tag + "\", " + childNodes.get(i).token.toJavascript() + ", function(print) {");
                        childNodes.get(i).toJavascript(preamble, buffer);
                        buffer.append("})");
                    }
                    buffer.append("]), " + token.toJavascript() + ").render("+JSWriter.CONTEXT_STACK_VAR+")");
                    buffer.append(");\n");
                }
            };
        }
        public Map<String, JSFunction> getHelpers() {
            return new HashMap<String, JSFunction>();
        }
    }
}
