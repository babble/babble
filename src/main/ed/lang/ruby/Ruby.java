// Ruby.java

package ed.lang.ruby;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;

public class Ruby {
    
    public static final String RUBY_V_CALL = "_rubyVCall";
    public static final String RUBY_CV_CALL = "_rubyCVCall";
    public static final String RUBY_NEW = "_rubyNew";

    public static final String RUBY_NEWNAME = "_____rnew___";

    public static void install( Scope s ){
        
        s.put( RUBY_V_CALL , new JSFunctionCalls1(){
                public Object call( Scope s , Object foo , Object extra[] ){

                    if ( foo == null )
                        return null;
                    
                    if ( foo instanceof JSFunction )
                        return ((JSFunction)foo).call( s );
                    
                    return foo;
                }
            } , true );

        final JSFunctionCalls2 _cvCall = 
            new JSFunctionCalls2(){
                public Object call( Scope s , Object thing , Object funcName , Object extra[] ){
                    
                    if ( thing == null )
                        throw new NullPointerException();

                    if ( ! ( thing instanceof JSObjectBase) )
                        throw new RuntimeException( "problem (" + thing.getClass() + ")" );
                    
                    JSObject jo = (JSObject)thing;
                    
                    Object func = jo.get( funcName );
                    
                    if ( func == null )
                        throw new NullPointerException();
                    
                    if ( ! ( func instanceof JSFunction ) )
                        return func;
                    
                    JSFunction f = (JSFunction)func;
                    return f.callAndSetThis( s , thing , null );
                }
            };
        
        s.put( RUBY_CV_CALL , _cvCall , true );
        
        s.put( RUBY_NEW , new JSFunctionCalls1(){
                public Object call( Scope s , Object thing , Object extra[] ){
                    if ( thing == null )
                        throw new NullPointerException( "need a function or a constructor" );
                    
                    if ( ! ( thing instanceof JSFunction ) )
                        return _cvCall.call( s , thing , RUBY_NEWNAME , extra );
                    
                    JSObjectBase o = new JSObjectBase();
                    o.setConstructor( (JSFunction)thing , true , extra );
                    return o;
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
