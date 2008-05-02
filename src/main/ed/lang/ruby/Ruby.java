// Ruby.java

package ed.lang.ruby;

import java.util.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.appserver.*;

public class Ruby {
    
    public static final String RUBY_V_CALL = "_rubyVCall";
    public static final String RUBY_CV_CALL = "_rubyCVCall";
    public static final String RUBY_NEW = "_rubyNew";
    public static final String RUBY_INCLUDE = "_rinclude";
    
    public static final String RUBY_NEWNAME = "_____rnew___";
    public static final String RUBY_SHIFT = "__rshift";
    public static final String RUBY_PRIVATE = "__rprivate";
    public static final String RUBY_REQUIRE = "__rrequire";
    public static final String RUBY_RAISE = "__rraise";

    static final Map<String,String> _nameMapping = new TreeMap<String,String>();
    static {
        _nameMapping.put( "new" , RUBY_NEWNAME );
        _nameMapping.put( "private" , RUBY_PRIVATE );
        _nameMapping.put( "<<" , RUBY_SHIFT );
        _nameMapping.put( "require" , RUBY_REQUIRE );
        _nameMapping.put( "raise" , RUBY_RAISE );
    }

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

                    if ( ! ( thing instanceof JSObject) )
                        throw new RuntimeException( "problem (" + thing.getClass() + ")" );
                    
                    if ( funcName == null )
                        throw new NullPointerException( "funcName can't be null" );

                    JSObject jo = (JSObject)thing;
                    
                    Object func = jo.get( RubyConvert._mangleFunctionName( funcName.toString() ) );
                    
                    if ( func == null )
                        return null;
                    
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

        s.put( RUBY_INCLUDE , new JSFunctionCalls1(){
                public Object call( Scope s , Object thing , Object extra[] ){
                    if ( thing == null )
                        throw new NullPointerException( "tried to include a null thing" );
                    
                    if ( thing instanceof JSObject ){
                        JSObject o = (JSObject)thing;
                        for ( String key : o.keySet() )
                            s.set( key , o.get( key ) );

                        Object incObj = o.get( "included" );
                        if ( incObj != null && incObj instanceof JSFunction )
                            ((JSFunction)incObj).call( s , s.getThis() );
                        
                        return null;
                    }

                    throw new RuntimeException( "don't know what to do ");
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
        
        
        s.put( RUBY_PRIVATE , new JSFunctionCalls0(){
                public Object call( Scope s , Object symbols[] ){
                    return null;
                }
            } , true );

        s.put( RUBY_REQUIRE , new JSFunctionCalls1(){
                public Object call( Scope s , Object pathObj , Object extra[] ){
                    if ( pathObj == null )
                        throw new NullPointerException( "can't send require nothing" );

                    String path = pathObj.toString();
                    int idx = path.lastIndexOf( "." );
                    if ( idx > 0 )
                        path = path.substring( 0 , idx );

                    return ((JSFileLibrary)s.get( "__path__" )).getFromPath( path );
                }
            } , true );

        s.put( RUBY_RAISE , new JSFunctionCalls0(){
                public Object call( Scope s , Object extra[] ){
                    throw new RuntimeException( "blah" );
                } 
            } , true );
                
    }
}
