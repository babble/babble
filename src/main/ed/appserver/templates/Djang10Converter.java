// Djang10Converter.java

package ed.appserver.templates;

import java.util.*;

import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSString;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls2;


public class Djang10Converter extends HtmlLikeConverter {
	private static final String DJANGO_VAR_EXPAND = "_djangoVarExpand";
	
    public Djang10Converter(){
        super( "djang10" , _codeTags );
    }

    protected boolean wants( Template t ){
        return t.getName().endsWith( ".djang10" );
    }

    protected String getNewName( Template t ){
        return t.getName().replaceAll( "\\.(djang10)$" , "_$1.js" );
    }

    @Override
    protected void start(Generator g) {
    	g.append( "var obj = arguments[0];\n" );
    	g.append(
    			"var _expandVarFn = function(varName) {\n" +
    			"	var result = obj;\n" +
				"	varName.split(/\\./).every(function(varNamePart) { \n" +
    			"		if(result == null)\n"+
    			"			return false;\n" +
    			
    			"		if(result[varNamePart] instanceof Function)\n" +
    			"			result = result[varNamePart]();\n" +
				"		else\n" +
				"			result = result[varNamePart];\n" +
				"		return true;\n"+
				"	});\n" +

				"   if((result instanceof Array) && result.length == 0)\n" +
				"		return \"\";\n" +
				"	if((result instanceof Object) && !(result instanceof String) && result.keySet().length == 0)\n"+
				"		return \"\";\n" +
				"	return result || \"\";\n" +
				"};\n"
    	);
    	
    	super.start(g);
    }
    
    protected void gotCode( Generator g , CodeMarker cm , String code ){
        
    	if ( cm._startTag.equals( "{{" ) ){
            g.append( "print( _expandVarFn(  \"" + code.trim() + "\" ) )\n");
            return;
        }

        throw new RuntimeException( "can't handle : " + cm._startTag );
    }

    protected boolean gotStartTag( Generator g , String tag , State state ){
        return false;
    }
    
    protected boolean gotEndTag( Generator g , String tag , State state ){
        return false;
    }
    
    static List<CodeMarker> _codeTags = new ArrayList<CodeMarker>();
    static {
        _codeTags.add( new CodeMarker( "{{" , "}}" ) );
        //_codeTags.add( new CodeMarker( "{" , "}" ) );

    }
}
