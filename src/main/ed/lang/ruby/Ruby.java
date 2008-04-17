// Ruby.java

package ed.lang.ruby;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;

public class Ruby {
    
    public static final String RUBY_V_CALL = "_rubyVCall";
    public static final String RUBY_CV_CALL = "_rubyCVCall";

    public static void install( Scope s ){
        
        s.put( RUBY_V_CALL , new JSFunctionCalls1(){
                public Object call( Scope s , Object foo , Object extra[] ){
                    
                    System.err.println( "foo : " + foo );

                    if ( foo == null )
                        return null;
                    
                    if ( foo instanceof JSFunction )
                        return ((JSFunction)foo).call( s );
                    
                    return foo;
                }
            } , true );

        s.put( RUBY_CV_CALL , new JSFunctionCalls2(){
                public Object call( Scope s , Object thing , Object funcName , Object extra[] ){
                    
                    if ( ! ( thing instanceof JSObjectBase) )
                        throw new RuntimeException( "problem" );
                    
                    JSObject jo = (JSObject)thing;
                    
                    Object func = jo.get( funcName );

                    if ( func == null )
                        throw new NullPointerException();
                    
                    if ( ! ( func instanceof JSFunction ) )
                        return func;

                    JSFunction f = (JSFunction)func;
                    return f.callAndSetThis( s , thing , null );
                }
            } , true );

        s.put( "attr_accessor" , new JSFunctionCalls0(){
                public Object call( Scope s , Object symbols[] ){
                    JSObjectBase job = (JSObjectBase)s.getThis();
                    if ( job == null )
                        throw new NullPointerException( "no this and attr_accessor needs it" );
                    return null;
                }
            } , true );
    }
}
