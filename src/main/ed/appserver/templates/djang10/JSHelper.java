/**
 * 
 */
package ed.appserver.templates.djang10;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ed.appserver.JSFileLibrary;
import ed.appserver.templates.Djang10Converter;
import ed.appserver.templates.djang10.Variable.FilterSpec;
import ed.appserver.templates.djang10.filters.Filter;
import ed.appserver.templates.djang10.tagHandlers.TagHandler;
import ed.js.JSFunction;
import ed.js.JSObjectBase;
import ed.js.JSString;
import ed.js.engine.JSCompiledScript;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;
import ed.js.func.JSFunctionCalls2;

public class JSHelper extends JSObjectBase {

	public static final String ADD_TEMPLATE_ROOT = "addTemplateRoot";
	public static final String CALL_PATH = "callPath";
	public static final String VAR_EXPAND = "djangoVarExpand";
	public static final String NS = "__djang10";

	
	private ArrayList<JSFileLibrary> templateRoots;
	
	public JSHelper() {
		templateRoots = new ArrayList<JSFileLibrary>();
		
		//add the template helpers
		Map<String, JSFunction> helpers = new HashMap<String, JSFunction>();
    	for (TagHandler tagHandler : Djang10Converter.getTagHandlers().values()) {
    		helpers.putAll(tagHandler.getHelpers());
		}
    	for(String name : helpers.keySet()) {
    		this.set(name, helpers.get(name));
    	}
    	
    	//add the basic helpers
    	this.set(JSHelper.VAR_EXPAND, varExpand);
    	this.set(JSHelper.CALL_PATH, callPath);
    	this.set(JSHelper.ADD_TEMPLATE_ROOT, addTemplateRoot);
    	
    	this.lock();
	}
	
	private final JSFunction varExpand = new JSFunctionCalls2() {
		@Override
		public Object call(Scope scope, Object varName, Object defaultValue, Object[] extra) {
			Object value = null;
			
			
			Variable variable = Parser.parseVariable(((JSString)varName).toString());
			try {
				value = Djang10Converter.resolveVariable(scope, variable.base);
			} catch(NoSuchFieldError e) {
				value = Variable.UNDEFINED_VALUE;
			}
			
			for(FilterSpec filterSpec : variable.filters) {
				Filter filter = Djang10Converter.getFilters().get(filterSpec.name);
				
				Object paramValue = Djang10Converter.resolveVariable(scope, filterSpec.param);
				
				value = filter.apply(value, paramValue);
			}
			
			return value == null || value == Variable.UNDEFINED_VALUE? defaultValue : value;
		}
	};
	
	private final JSFunction callPath = new JSFunctionCalls1() {
		@Override
		public Object call(Scope scope, Object pathObj, Object[] extra) {
			
			if(pathObj instanceof JSCompiledScript)
				return ((JSCompiledScript)pathObj).call(scope.child(), extra);

			String path = ((JSString)pathObj).toString().trim().replaceAll("/+", "/").replaceAll("\\.\\w*$", "");
			JSCompiledScript target = null;
			
			if(path.startsWith("/")) {
				String[] newRootPathParts = path.split("/", 3);
				if(newRootPathParts.length != 3 || newRootPathParts[2].trim().length() == 0)
					throw new IllegalArgumentException("Invalid path");
				
				//get the root filelib/ ie: local, core, etc
				String newRootBasePath = newRootPathParts[1];
				Object newRootBaseObj = scope.get(newRootBasePath);
				if(!(newRootBaseObj instanceof JSFileLibrary))
					throw new IllegalArgumentException("Path not found");
				
				//get the actual file
				Object targetObj = ((JSFileLibrary)newRootBaseObj).getFromPath(newRootPathParts[2]);
				if(!(targetObj instanceof JSCompiledScript ))
					throw new IllegalArgumentException("File not found");

				target = (JSCompiledScript)targetObj;
			}
			else {
				for(int i = templateRoots.size()-1; i>= 0; i--) {
					JSFileLibrary fileLibrary = templateRoots.get(i);
					
					Object targetObj = fileLibrary.getFromPath(path);
					
					if(targetObj instanceof JSCompiledScript) {
						target = (JSCompiledScript)targetObj;
						break;
					}
				}
				if(target == null)
					throw new IllegalArgumentException("File not found");
			}
			
			return target.call(scope.child(), extra);
		}
	};
	

	
	private final JSFunction addTemplateRoot = new JSFunctionCalls1() {
		@Override
		public Object call(Scope scope, Object newRoot, Object[] extra) {
			JSFileLibrary templateFileLib;

			if(newRoot instanceof JSString) {
				String newRootPath = newRoot.toString().trim().replaceAll("/+", "/");
				if(!newRootPath.startsWith("/"))
					throw new IllegalArgumentException("Only Absolute paths are allowed");
				
				String[] newRootPathParts = newRootPath.split("/", 3);
				
				//find the base file lib
				Object templateFileLibObj = scope.get(newRootPathParts[1]);
				if(!(templateFileLibObj instanceof JSFileLibrary))
					throw new IllegalArgumentException("Path not found");
				templateFileLib = (JSFileLibrary)templateFileLibObj;
				
				if(newRootPathParts.length == 3 && newRootPathParts[2].length() > 0) {
					templateFileLibObj = templateFileLib.getFromPath(newRootPathParts[2]);

					if(!(templateFileLibObj instanceof JSFileLibrary))
						throw new IllegalArgumentException("Path not found");
					
					templateFileLib = (JSFileLibrary)templateFileLibObj;
				}
			}
			else if(newRoot instanceof JSFileLibrary) {
				templateFileLib = (JSFileLibrary)newRoot;
			}
			else {
				throw new IllegalArgumentException("Only Paths and FileLibraries are accepted");
			}
			
			templateRoots.add(templateFileLib);
			return null;
		}
	};
}