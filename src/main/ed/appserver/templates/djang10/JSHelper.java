/**
 * 
 */
package ed.appserver.templates.djang10;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ed.appserver.JSFileLibrary;
import ed.appserver.templates.Djang10Converter;
import ed.appserver.templates.djang10.tagHandlers.TagHandler;
import ed.appserver.templates.djang10.tagHandlers.VariableTagHandler;
import ed.js.JSFunction;
import ed.js.JSObjectBase;
import ed.js.JSString;
import ed.js.engine.JSCompiledScript;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;

public class JSHelper extends JSObjectBase {

	public static final String ADD_TEMPLATE_ROOT = "addTemplateRoot";
	public static final String CALL_PATH = "callPath";
	public static final String LOAD_PATH = "loadPath";
	public static final String CONTEXT_CLASS = "Context";
	public static final String NS = "__djang10";
	
	private ArrayList<JSFileLibrary> templateRoots;
	
	public JSHelper() {
		templateRoots = new ArrayList<JSFileLibrary>();
		
		//add the template helpers
		Map<String, JSFunction> helpers = new HashMap<String, JSFunction>();
    	for (TagHandler tagHandler : Djang10Converter.getTagHandlers().values()) {
    		helpers.putAll(tagHandler.getHelpers());
		}
    	helpers.putAll(Djang10Converter.getVariableTagHandler().getHelpers());
    	
    	for(String name : helpers.keySet()) {
    		this.set(name, helpers.get(name));
    	}
    	
    	//add the basic helpers
    	this.set(JSHelper.LOAD_PATH, loadPath);
    	this.set(JSHelper.CALL_PATH, callPath);
    	this.set(JSHelper.ADD_TEMPLATE_ROOT, addTemplateRoot);
    	this.set(JSHelper.CONTEXT_CLASS, Context.CONSTRUCTOR);
    	
    	this.lock();
	}
	
	private final JSFunction callPath = new JSFunctionCalls1() {
		@Override
		public Object call(Scope scope, Object pathObj, Object[] extra) {
			Object loadedObj = loadPath.call(scope, pathObj, extra);
			
			if(loadedObj instanceof JSCompiledScript)
				return ((JSCompiledScript)loadedObj).call(scope.child(), extra);
			
			return null;
		}
	};
	
	private final JSFunction loadPath = new JSFunctionCalls1() {
		@Override
		public Object call(Scope scope, Object pathObj, Object[] extra) {
			
		    if(pathObj == null || pathObj == VariableTagHandler.UNDEFINED_VALUE)
		        return null;
		    
		    if(pathObj instanceof JSCompiledScript)
	            return pathObj;

			String path = ((JSString)pathObj).toString().trim().replaceAll("/+", "/").replaceAll("\\.\\w*$", "");
			JSCompiledScript target = null;
			
			if(path.startsWith("/")) {
				String[] newRootPathParts = path.split("/", 3);
				if(newRootPathParts.length < 2 || newRootPathParts[1].trim().length() == 0)
				    return null;
				
				String newRootBasePath = newRootPathParts[1];
				Object newRootBaseObj = null;
				if(newRootPathParts.length == 3) {
    				newRootBaseObj = scope.get(newRootBasePath);
    				if(!(newRootBaseObj instanceof JSFileLibrary))
    					newRootBaseObj = null;
				}
				

				if(newRootBaseObj == null) {

	                //fallback on resolving absolute paths against site
				    newRootBaseObj = ((JSFileLibrary)scope.get("local")).getFromPath(path);
				    
				    return (newRootBaseObj instanceof JSCompiledScript)? newRootBaseObj : null;
				}
				else {
	                Object targetObj = ((JSFileLibrary)newRootBaseObj).getFromPath(newRootPathParts[2]);
	                
	                return (targetObj instanceof JSCompiledScript)? targetObj : null;
				}
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
					return null;
			}
			
			return target;
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