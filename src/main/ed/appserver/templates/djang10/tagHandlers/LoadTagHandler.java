package ed.appserver.templates.djang10.tagHandlers;

import java.util.HashMap;
import java.util.Map;

import ed.appserver.jxp.JxpSource;
import ed.appserver.templates.djang10.JSHelper;
import ed.appserver.templates.djang10.Library;
import ed.appserver.templates.djang10.Node;
import ed.appserver.templates.djang10.Parser;
import ed.appserver.templates.djang10.TemplateException;
import ed.appserver.templates.djang10.Parser.Token;
import ed.appserver.templates.djang10.filters.Filter;
import ed.appserver.templates.djang10.generator.JSWriter;
import ed.js.JSFunction;
import ed.js.engine.Scope;

public class LoadTagHandler implements TagHandler {
    private static final String MODULE_LIST = "moduleList";

    public Node compile(Parser parser, String command, Token token) throws TemplateException {

        String moduleName = token.contents.trim().split("\\s+", 2)[1];
        if (!moduleName.matches("^\\w+$"))
            throw new TemplateException("invalid module name: " + moduleName);

        Scope callingScope = Scope.getThreadLocal();
        JSHelper helper = (JSHelper) callingScope.get(JSHelper.NS);
        JSFunction module = helper.loadModule(moduleName);
        Library moduleLibrary = helper.callModule(callingScope, moduleName);

        parser.getTracker().addDependency((JxpSource) module.get(JxpSource.JXP_SOURCE_PROP));

        if (module == null)
            throw new TemplateException("failed to find module: " + moduleName);

        LoadTagStateVar stateVar = parser.getStateVariable(getClass());
        boolean isFirst = false;
        if (stateVar == null) {
            stateVar = new LoadTagStateVar();
            parser.setStateVariable(getClass(), stateVar);
            isFirst = true;
        }

        // pull out the tags
        /*
         * for(String tagName : module.getTags().keySet()) {
         * stateVar.tagLibraryIndex.put(tagName, stateVar.libraryCount);
         * parser.getFilters().put(tagName, new
         * ModuleFilterWrapper(stateVar.libraryCount)); }
         */

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
        return new HashMap<String, JSFunction>();
    }

    private static class LoadTagStateVar {
        public final Map<String, Integer> tagLibraryIndex = new HashMap<String, Integer>();
        public final Map<String, Integer> filterLibraryIndex = new HashMap<String, Integer>();
        public int libraryCount = 0;
    }

    private static final class LoadNode extends Node {
        private final String moduleName;
        private final boolean isFirst;

        public LoadNode(Token token, String moduleName, boolean isFirst) {
            super(token);
            this.moduleName = moduleName;
            this.isFirst = isFirst;
        }

        public void getRenderJSFn(JSWriter preamble, JSWriter buffer) throws TemplateException {

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
}
