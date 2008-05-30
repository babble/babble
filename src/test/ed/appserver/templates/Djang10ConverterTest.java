// Djang10ConverterTest.java

package ed.appserver.templates;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.testng.annotations.Factory;

import ed.appserver.JSFileLibrary;
import ed.appserver.templates.djang10.JSHelper;
import ed.js.JSArray;
import ed.js.JSDate;
import ed.js.JSFunction;
import ed.js.JSObjectBase;
import ed.js.JSString;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls0;
import ed.js.func.JSFunctionCalls1;

public class Djang10ConverterTest extends ConvertTestBase {

    public Djang10ConverterTest(){
        super( ".djang10" );
    }


    @Factory 
    public Object[] getAllTests(){
        return _all.toArray();
    }

    Djang10Converter getConverter(){
        return new Djang10Converter();
    }

    @Override
    Object[] getArgs(Scope testScope){
        JSObjectBase o = new JSObjectBase();
        o.set( "foo" , "17" );
        o.set( "a" , "A" );
        o.set( "b" , "B" );
        o.set( "c" , "A" );
        
        JSObjectBase nested = new JSObjectBase();
        final JSObjectBase nested3 = new JSObjectBase();
        JSFunction nested2Fn = new JSFunctionCalls0() {
			@Override
			public Object call(Scope scope, Object[] extra) {
				return nested3;
			}
        	
        };
        
        o.set("nested", nested);
        nested.set("nested2Fn", nested2Fn);
        nested3.set("last", "moo");
        
        
        JSArray array = new JSArray();
        for(int i=0; i<5; i++)
        	array.add(i);
        o.set("array", array);
        
        
        o.set("urlParam", "?\\/~!.,");
        
        Calendar cal = new GregorianCalendar();
        cal.set(1981, 12 - 1, 20, 15, 11, 37);

        
        o.set("date", new JSDate(cal));  
        
        o.set("includedTemplate", "/local/djang10-if");
        
        JSArray array2 = new JSArray();
        int[] array2values = new int[] { 5,4,3,2,1,6,7,8,9,10 };
        for(int val : array2values) {
        	JSObjectBase obj = new JSObjectBase();
        	obj.set("myProp", val);
        	array2.add(obj);
        }
        o.set("array2", array2);
        
        JSFileLibrary localLib = (JSFileLibrary)testScope.get("local");
        o.set("includedTemplateJsFunction", localLib.get("djang10-if"));
        
        Djang10Converter.injectHelpers(testScope);
        JSFunction addTemplateRoot = (JSFunction)((JSObjectBase)testScope.get(JSHelper.NS)).get(JSHelper.ADD_TEMPLATE_ROOT);
        addTemplateRoot.call(testScope, new JSString("/local"));
        
        
        o.set("echoFunc", new JSFunctionCalls1() {
           public Object call(Scope scope, Object in, Object[] extra) {
               return in;
           }
        });
        
        return new Object[]{ o };
    }
    
    public static void main( String args[] ){
        Djang10ConverterTest t = new Djang10ConverterTest();
        t.runConsole();
    }
    
}
