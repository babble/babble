// Djang10ConverterTest.java

package ed.appserver.templates;

import org.testng.annotations.Factory;

import ed.js.JSArray;
import ed.js.JSFunction;
import ed.js.JSObjectBase;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls0;

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
    Object[] getArgs(){
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
        
        return new Object[]{ o };
    }
    
    public static void main( String args[] ){
        Djang10ConverterTest t = new Djang10ConverterTest();
        t.runConsole();
    }
    
}
